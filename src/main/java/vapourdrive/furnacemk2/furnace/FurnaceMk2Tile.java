package vapourdrive.furnacemk2.furnace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.config.ConfigSettings;
import vapourdrive.furnacemk2.furnace.itemhandlers.*;
import vapourdrive.furnacemk2.items.IExperienceStorage;
import vapourdrive.furnacemk2.utils.FurnaceUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static vapourdrive.furnacemk2.setup.Registration.FURNACEMK2_TILE;

public class FurnaceMk2Tile extends BlockEntity {

    private final FurnaceAugmentHandler augmentHandler = new FurnaceAugmentHandler(this, AUGMENT_SLOTS.length);
    private final FurnaceFuelHandler fuelHandler = new FurnaceFuelHandler(this, FUEL_SLOT.length);
    private final FurnaceIngredientHandler ingredientHandler = new FurnaceIngredientHandler(this, INPUT_SLOT.length);
    private final FurnaceExperienceHandler experienceHandler = new FurnaceExperienceHandler(this, EXPERIENCE_OUTPUT_SLOTS.length);
    private final FurnaceOutputHandler outputHandler = new FurnaceOutputHandler(this, OUTPUT_SLOTS.length);
    private final LazyOptional<FurnaceOutputHandler> lazyOutputHandler = LazyOptional.of(() -> outputHandler);
    private final CombinedInvWrapper combined = new CombinedInvWrapper(augmentHandler, fuelHandler, ingredientHandler, outputHandler, experienceHandler);
    private final LazyOptional<CombinedInvWrapper> combinedHandler = LazyOptional.of(() -> combined);

    private ItemStack lastSmelting = ItemStack.EMPTY;

    public final int maxExp = 50000;
    public final int maxFuel = 15000000;
    public int wait = 20;
    public int toAdd = 0;
    public int increment = 0;

    private ItemStack currentResult = ItemStack.EMPTY;
    private ItemStack currentIngredient = ItemStack.EMPTY;

    private ItemStack currentFuel = ItemStack.EMPTY;
    private int currentBurn = 0;

    public final FurnaceData furnaceData = new FurnaceData();

    enum Area {
        AUGMENT,
        FUEL,
        INPUT,
        OUTPUT,
        XP
    }

    public static final int[] AUGMENT_SLOTS = {0, 1, 2};
    public static final int[] FUEL_SLOT = {0};
    public static final int[] INPUT_SLOT = {0};
    public static final int[] OUTPUT_SLOTS = {0, 1, 2, 3};
    public static final int[] EXPERIENCE_OUTPUT_SLOTS = {0};

    public FurnaceMk2Tile(BlockPos pos, BlockState state) {
        super(FURNACEMK2_TILE.get(), pos, state);
    }

    public void tickServer(BlockState state) {
        ItemStack fuel = getStackInSlot(Area.FUEL, 0);
        ItemStack ingredient = getStackInSlot(Area.INPUT, 0);
        doFuelProcess(fuel);

        //Reset the cook progress if it's a new item
        if (!lastSmelting.isEmpty() && !ItemStack.isSame(ingredient, lastSmelting)) {
            furnaceData.cookProgress = 0;
        }
        //keep track of the current item to check next tick
        lastSmelting = ingredient.copy();

        if(!ingredient.isEmpty()) {
            doCookProcesses(ingredient, state);
        }

        doExperienceItemProcesses();
    }

    private void doFuelProcess(ItemStack fuel) {
        if (wait >= 10) {
            toAdd = tryConsumeFuel(fuel);
            if(toAdd+furnaceData.fuel>this.getMaxFuel()){
                toAdd = this.getMaxFuel()-furnaceData.fuel;
            }
            increment = toAdd/10;
            wait = 0;
        }
        else {
            wait +=1;
            if(toAdd > 0){
                furnaceData.fuel+=increment;
                toAdd -= increment;
            }
        }
    }

    private void doCookProcesses(ItemStack ingredient, BlockState state) {
        if (furnaceData.cookProgress == 0){
            if(currentResult.isEmpty()) {
                currentResult = FurnaceUtils.getSmeltingResultForItem(level, ingredient);
                currentIngredient = ingredient;
                furnaceData.cookMax = FurnaceUtils.getCookTime(level, ingredient);
            }

            if(pushOutput(currentResult, true) >= 1 && furnaceData.fuel >= furnaceData.cookMax){
                assert level != null;
                level.setBlock(worldPosition, state.setValue(BlockStateProperties.LIT, true), Block.UPDATE_ALL);
                this.setChanged();
                progressCook();
            }
        } else if (furnaceData.cookProgress >= 0) {
            progressCook();
            if (furnaceData.cookProgress >= furnaceData.cookMax) {
                if (pushOutput(currentResult, false) == -1) {
                    depositExperience(ingredient);
                    //ingredientHandler.extractItem(INPUT_SLOT[0], 1, false);
                    removeFromSlot(Area.INPUT, 0, 1, false);
                    ItemStack remainingIngredient = getStackInSlot(Area.INPUT,0);
                    if(remainingIngredient.isEmpty()){
                        currentIngredient = ItemStack.EMPTY;
                        currentResult = ItemStack.EMPTY;

                    } else if (!ItemStack.isSame(remainingIngredient, currentIngredient)) {
                        currentResult = ItemStack.EMPTY;
                        furnaceData.cookMax = 0;
                    }
                    if(remainingIngredient.isEmpty() || furnaceData.fuel < furnaceData.cookMax) {
                        assert level != null;
                        level.setBlock(worldPosition, state.setValue(BlockStateProperties.LIT, false), Block.UPDATE_ALL);
                        this.setChanged();
                    }
                }
                furnaceData.cookProgress = 0;
            }
        }
    }

    private void doExperienceItemProcesses() {
        ItemStack experienceStorage = getStackInSlot(Area.XP, 0);
        if(!experienceStorage.isEmpty() && experienceStorage.getItem() instanceof IExperienceStorage xpStack) {
            if (furnaceData.experience >= 100) {
                extractExperience(experienceStorage);
            }
            if (xpStack.getCurrentExperienceStored(experienceStorage) >= xpStack.getMaxExperienceStored(experienceStorage)) {
                FurnaceMk2.debugLog("Pushing output for full experience crystal");
                if (pushOutput(experienceStorage, false) == -1) {
                    removeFromSlot(Area.XP, 0, 1, false);
                    //experienceHandler.extractItem(EXPERIENCE_OUTPUT_SLOTS[0], 1, false);
                }
            }
        }
    }

    public void extractExperience(ItemStack stack) {
        IExperienceStorage xpStack = (IExperienceStorage) stack.getItem();
        int toSend = xpStack.receiveExperience(stack, 1, true);
        if (toSend == 1) {
            xpStack.receiveExperience(stack, 1, false);
            furnaceData.experience -= 100;
        }
    }

    public void progressCook() {
        double speed = getSpeedMultiplier();
        if (furnaceData.fuel > 0) {
            furnaceData.cookProgress += (int) (100 * speed);
            furnaceData.fuel -= (int) (100 * speed);
        }
    }

    public int tryConsumeFuel(ItemStack fuel) {
        FurnaceMk2.debugLog("  Trying to consume fuel");
        if (!fuel.isEmpty()) {
            if(currentFuel.isEmpty() || !ItemStack.isSame(currentFuel, fuel)){
                currentFuel = fuel.copy();
                currentBurn = (int)(getBurnDuration(fuel)*getEfficiencyMultiplier());
            }

            if(furnaceData.fuel+currentBurn*getEfficiencyMultiplier() <= this.maxFuel || furnaceData.fuel< furnaceData.cookMax) {
                if (currentFuel.hasCraftingRemainingItem()) {
//                FurnaceMk2.debugLog("Fuel has a container item to try to push.");
                    ItemStack fuelRemainder = currentFuel.getCraftingRemainingItem();
                    if (canPushAllOutputs(new ItemStack[]{fuelRemainder})) {
//                    FurnaceMk2.debugLog("Either the ingedient or the bucket say there's room for two");
                        pushOutput(fuelRemainder, false);
                    } else {
                        return 0;
                    }
                }
                FurnaceMk2.debugLog("  Burn flag true");
                FurnaceMk2.debugLog("  To add: " + currentBurn);
                FurnaceMk2.debugLog("  Current fuel: " + furnaceData.fuel);
                FurnaceMk2.debugLog("  Removing fuel");
                removeFromSlot(Area.FUEL, 0, 1, false);
                if (!ItemStack.isSame(currentFuel, fuel)) {
                    currentFuel = ItemStack.EMPTY;
                    currentBurn = (int) (getBurnDuration(fuel) * getEfficiencyMultiplier());
                }
//                furnaceData.fuel += toAdd;
                return currentBurn;
            }
        }
        return 0;
    }

    public boolean canPushAllOutputs (ItemStack[] stacks) {
        int empties = getEmptyOutputSlotCount();
        if (empties >= stacks.length) {
            return true;
        }
        else if (empties == 0) {
            for (ItemStack stack:stacks) {
                if (pushOutput(stack, true) < 1){
                    return false;
                }
            }
        }
        else {
            int eligible = 0;
            for (ItemStack stack:stacks) {
                for (int i : OUTPUT_SLOTS) {
                    if (!getStackInSlot(Area.OUTPUT, i).isEmpty()){
                        FurnaceMk2.debugLog("  Trying to output to non-empty slot: " + i);
                        if (insertToSlot(Area.OUTPUT, i, stack, true) == ItemStack.EMPTY){
                        //if (outputHandler.internalInsertItem(i, stack, true) == ItemStack.EMPTY) {
                            FurnaceMk2.debugLog("    Match, Furnace output: " + i + ": " + getStackInSlot(Area.OUTPUT, i));
                            eligible++;
                        }
                    }
                }
            }
            FurnaceMk2.debugLog("Available: "+ eligible + ", Empty: "+ empties);
            return empties + eligible >= stacks.length;
        }

        return true;
    }

    public int getEmptyOutputSlotCount() {
        int empty = 0;
        for (int i : OUTPUT_SLOTS) {
            if (getStackInSlot(Area.OUTPUT, i).isEmpty()){
                empty++;
            }
        }
        return empty;
    }

    protected int getBurnDuration(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        } else {
            //everything is multiplied by 100 for variable increments instead of 1 per tick
            //i.e 100% efficiency is 100 consumption per tick, 125% is 80 consumption etc
            return net.minecraftforge.common.ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) * 100;
        }
    }

    //if simulated, return the max of the open or available slots
    //if not simulated
    public int pushOutput(ItemStack ingredient, boolean simulate) {
        FurnaceMk2.debugLog("############### Pushing Output #################");
        int available = 0;
        int empty = 0;

        ItemStack result= ingredient.copy();

        //iterates through non-empty slots
        FurnaceMk2.debugLog("############ Starting non-empties ##############");
        for (int i : OUTPUT_SLOTS) {
            if (!getStackInSlot(Area.OUTPUT, i).isEmpty()) {
                FurnaceMk2.debugLog("  Trying to output to non-empty slot: " + i);
                if (insertToSlot(Area.OUTPUT, i, result, simulate) == ItemStack.EMPTY) {
                    FurnaceMk2.debugLog("    Match, Furnace output: " + i + " " + getStackInSlot(Area.OUTPUT, i));
                    if(!simulate) {
                        return -1;
                    }
                    available++;
                }
            }
        }

        FurnaceMk2.debugLog("  -- Available non-empty: " + available);

        //iterate through the slots (empty or not)
        FurnaceMk2.debugLog("############ Starting empties ##############");
        for (int i : OUTPUT_SLOTS) {
            if (getStackInSlot(Area.OUTPUT, i).isEmpty()) {
                FurnaceMk2.debugLog("  Trying to output to slot: " + i);
                if (insertToSlot(Area.OUTPUT, i, result, simulate) == ItemStack.EMPTY) {
                    FurnaceMk2.debugLog("    Furnace output: " + i + " " + getStackInSlot(Area.OUTPUT, i));
                    if (!simulate) {
                        return -1;
                    }
                    empty++;
                }
            }
        }
        if(empty == 0) {
            //FurnaceMk2.debugLog("No empty slots, " + available + " available slots");
            return Math.min(available, 1);
        }

        //FurnaceMk2.debugLog("Total viable slots: " + sum);
        return available + empty;
    }

    private void depositExperience(ItemStack ingredient) {
        double adjustment = getExperienceMultiplier();
        int toAdd = (int)(FurnaceUtils.getExperience(level, ingredient)*100*adjustment);
        if(furnaceData.experience + toAdd > maxExp) {
            furnaceData.experience = maxExp;
        }
        else {
            furnaceData.experience += toAdd;
        }
    }

    @Override
    public void load(CompoundTag tag) {
        outputHandler.deserializeNBT(tag.getCompound("invOut"));
        augmentHandler.deserializeNBT(tag.getCompound("invAug"));
        fuelHandler.deserializeNBT(tag.getCompound("invFuel"));
        ingredientHandler.deserializeNBT(tag.getCompound("invIngr"));
        experienceHandler.deserializeNBT(tag.getCompound("invExp"));

        furnaceData.cookProgress = tag.getInt("cookProgress");
        furnaceData.cookMax = tag.getInt("cookMax");
        furnaceData.experience = tag.getInt("experience");
        furnaceData.fuel = tag.getInt("fuel");

        increment = tag.getInt("increment");
        toAdd = tag.getInt("toAdd");
        wait = tag.getInt("wait");

        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.put("invOut", outputHandler.serializeNBT());
        tag.put("invAug", augmentHandler.serializeNBT());
        tag.put("invFuel", fuelHandler.serializeNBT());
        tag.put("invIngr", ingredientHandler.serializeNBT());
        tag.put("invExp", experienceHandler.serializeNBT());

        tag.putInt("cookProgress", furnaceData.cookProgress);
        tag.putInt("cookMax", furnaceData.cookMax);
        tag.putInt("experience", furnaceData.experience);
        tag.putInt("fuel", furnaceData.fuel);
        tag.putInt("increment",increment);
        tag.putInt("toAdd", toAdd);
        tag.putInt("wait", wait);

    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.DOWN) {
                //FurnaceMk2.debugLog("Passing lazy output to bottom");
                return lazyOutputHandler.cast();
            }
//            else if (side == Direction.UP) {
//                FurnaceMk2.debugLog("Passing lazy ingredient to top");
//                return lazyIngredientHandler.cast();
//            }
//            else if (side != null) {
//                FurnaceMk2.debugLog("Passing lazy fuel to side: " +side);
//                return lazyFuelHandler.cast();
//            }
//            FurnaceMk2.debugLog("Passing combined with no side");
            return combinedHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    public IItemHandler getItemHandler () {
        return combined;
    }

//    public Component getDisplayName() {
//        return Component.translatable("block.furnacemk2.furnacemk2");
//    }
//
//    @Nullable
//    public AbstractContainerMenu createMenu(int window, Inventory playerInv, Player player) {
//        assert this.level != null;
//        return new FurnaceMk2Container(window, this.level, this.worldPosition, playerInv, player, this.furnaceData);
//    }

    public double getSpeedMultiplier(){
        return getStackInSlot(Area.AUGMENT, 1).isEmpty() ? ConfigSettings.FURNACE_BASE_SPEED.get() : ConfigSettings.FURNACE_UPGRADED_SPEED.get()*ConfigSettings.FURNACE_BASE_SPEED.get();
    }

    public double getEfficiencyMultiplier(){
        return getStackInSlot(Area.AUGMENT, 0).isEmpty() ? ConfigSettings.FURNACE_BASE_EFFICIENCY.get() : ConfigSettings.FURNACE_UPGRADED_EFFICIENCY.get()*ConfigSettings.FURNACE_BASE_EFFICIENCY.get();
    }

    public double getExperienceMultiplier(){
        return getStackInSlot(Area.AUGMENT, 2).isEmpty() ? ConfigSettings.FURNACE_BASE_EXPERIENCE.get() : ConfigSettings.FURNACE_UPGRADED_EXPERIENCE.get()*ConfigSettings.FURNACE_BASE_EXPERIENCE.get();
    }

    public int getMaxExp() {
        return this.maxExp;
    }

    public int getMaxFuel() {
        return this.maxFuel;
    }

    public FurnaceData getFurnaceData() {
        return furnaceData;
    }

    public ItemStack getStackInSlot(Area area, int index) {
        return switch(area){
            case AUGMENT -> augmentHandler.getStackInSlot(AUGMENT_SLOTS[index]);
            case XP -> experienceHandler.getStackInSlot(EXPERIENCE_OUTPUT_SLOTS[index]);
            case FUEL -> fuelHandler.getStackInSlot(FUEL_SLOT[index]);
            case INPUT -> ingredientHandler.getStackInSlot(INPUT_SLOT[index]);
            case OUTPUT -> outputHandler.getStackInSlot(OUTPUT_SLOTS[index]);
        };
    }

    public void removeFromSlot(Area area, int index, int amount, boolean simulate) {
        switch (area) {
            case AUGMENT -> augmentHandler.extractItem(AUGMENT_SLOTS[index], amount, simulate);
            case XP -> experienceHandler.extractItem(EXPERIENCE_OUTPUT_SLOTS[index], amount, simulate);
            case FUEL -> fuelHandler.extractItem(FUEL_SLOT[index], amount, simulate);
            case INPUT -> ingredientHandler.extractItem(INPUT_SLOT[index], amount, simulate);
            case OUTPUT -> outputHandler.extractItem(OUTPUT_SLOTS[index], amount, simulate);
        }
    }

    public ItemStack insertToSlot(Area area, int index, ItemStack stack, boolean simulate) {
        return switch(area){
            case AUGMENT -> augmentHandler.insertItem(AUGMENT_SLOTS[index], stack, simulate);
            case XP -> experienceHandler.insertItem(EXPERIENCE_OUTPUT_SLOTS[index], stack, simulate);
            case FUEL -> fuelHandler.insertItem(FUEL_SLOT[index], stack, simulate);
            case INPUT -> ingredientHandler.insertItem(INPUT_SLOT[index], stack, simulate);
            case OUTPUT -> outputHandler.insertItem(OUTPUT_SLOTS[index], stack, simulate, true);
        };
    }
}
