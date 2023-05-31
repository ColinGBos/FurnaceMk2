package vapourdrive.furnacemk2.furnace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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

    private boolean isBurning;
    private ItemStack lastSmelting = ItemStack.EMPTY;

    public final int maxExp = 50000;

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
        boolean isStarting = false;

        ItemStack fuel = getStackInSlot(Area.FUEL, 0);
        ItemStack ingredient = getStackInSlot(Area.INPUT, 0);

        //Reset the cook progress if it's a new item
        if (!lastSmelting.isEmpty() && !ItemStack.isSame(ingredient, lastSmelting)) {
            furnaceData.cookProgress = 0;
        }
        //keep track of the current item to check next tick
        lastSmelting = ingredient.copy();

        //Either not currently burning or resetting
        if(furnaceData.burnProgress == 0) {
            isStarting = doBurnInitiation(fuel, ingredient, state);
        }
        if(!ingredient.isEmpty()) {
            doCookProcesses(ingredient, isStarting);
        }

        doBurnProcesses(fuel, ingredient, state);
        doExperienceItemProcesses();
    }

    private boolean doBurnInitiation(ItemStack fuel, ItemStack ingredient, BlockState state) {
        //Check if there is something in the fuel slot
        if(!fuel.isEmpty() && !ingredient.isEmpty()) {
            if (tryStartBurning(fuel, ingredient)) {
                FurnaceMk2.debugLog("Starting burn");
                furnaceData.currentMaxBurn = getBurnDuration(fuel);
                //furnaceData.cookMax = FurnaceUtils.getCookTime(level, ingredient);
                //fuelHandler.extractItem(FUEL_SLOT[0], 1, false);
                removeFromSlot(Area.FUEL, 0, 1, false);
                isBurning = true;

                assert level != null;
                level.setBlock(worldPosition, state.setValue(BlockStateProperties.LIT, true), Block.UPDATE_ALL);
                this.setChanged();
                return true;
            }
        }
        return false;
    }

    private void doCookProcesses(ItemStack ingredient, boolean forced) {
        if ((furnaceData.cookProgress >= 0 && furnaceData.burnProgress > 0) || forced) {
            ItemStack result = FurnaceUtils.getSmeltingResultForItem(level, ingredient);
            //FurnaceMk2.debugLog("Pushing output to progress the cook");
            if(furnaceData.cookProgress == 0 || forced) {
                furnaceData.cookMax = FurnaceUtils.getCookTime(level, ingredient);
            }
            if(furnaceData.cookProgress > 0 || pushOutput(result, true) >= 1) {
                //FurnaceMk2.debugLog("Passed the first progress cook call");
                progressCook(false);
            }
            // complete the smelt
            if (furnaceData.cookProgress >= furnaceData.cookMax) {
                if (pushOutput(result, false) == -1) {
                    depositExperience(ingredient);
                    //ingredientHandler.extractItem(INPUT_SLOT[0], 1, false);
                    removeFromSlot(Area.INPUT, 0, 1, false);
                }
                furnaceData.cookProgress = 0;
                if (furnaceData.burnProgress == 0 || ingredient == ItemStack.EMPTY) {
                    furnaceData.cookMax = 0;
                }
            }
        }
        if(furnaceData.cookProgress > 0 && furnaceData.burnProgress == 0 && !forced) {
            progressCook(true);
        }
    }

    private void doBurnProcesses(ItemStack fuel, ItemStack ingredient, BlockState state) {
        if(isBurning || furnaceData.burnProgress > 0) {
            //FurnaceMk2.debugLog("Burn progress: " + furnaceData.burnProgress);
            progressBurn();
            if(furnaceData.burnProgress >= furnaceData.currentMaxBurn) {
                furnaceData.burnProgress = 0;
                furnaceData.currentMaxBurn = 0;
                if (!tryStartBurning(fuel, ingredient)) {
                    assert level != null;
                    level.setBlock(worldPosition, state.setValue(BlockStateProperties.LIT, false), Block.UPDATE_ALL);
                    isBurning = false;
                    this.setChanged();
                }
                else {
                    removeFromSlot(Area.FUEL, 0, 1, false);
                    //fuelHandler.extractItem(FUEL_SLOT[0], 1, false);
                    furnaceData.currentMaxBurn = getBurnDuration(fuel);
                }
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

    public void progressBurn() {
        double speed = getStackInSlot(Area.AUGMENT, 1).isEmpty() ? ConfigSettings.FURNACE_BASE_SPEED.get() : ConfigSettings.FURNACE_UPGRADED_SPEED.get()*ConfigSettings.FURNACE_BASE_SPEED.get();
        double efficiency = getStackInSlot(Area.AUGMENT, 0).isEmpty() ? speed : speed / (ConfigSettings.FURNACE_UPGRADED_EFFICIENCY.get()*ConfigSettings.FURNACE_BASE_EFFICIENCY.get());
        furnaceData.burnProgress += (int) (100 * efficiency);
    }

    public void progressCook(boolean reverse) {
        if(reverse) {
            if(furnaceData.cookProgress - 100 <= 0 ) {
                furnaceData.cookProgress = 0;
                return;
            }
            furnaceData.cookProgress -= 200;
            return;
        }
        double speed = getStackInSlot(Area.AUGMENT, 1).isEmpty() ? ConfigSettings.FURNACE_BASE_SPEED.get() : ConfigSettings.FURNACE_UPGRADED_SPEED.get()*ConfigSettings.FURNACE_BASE_SPEED.get();
        furnaceData.cookProgress += (int) (100 * speed);
    }

//    public boolean tryStartBurning(ItemStack fuel, ItemStack ingredient) {
//        if (!fuel.isEmpty() && !ingredient.isEmpty()) {
//            ItemStack result = FurnaceUtils.getSmeltingResultForItem(level, ingredient);
//            if (fuel.getContainerItem() == ItemStack.EMPTY || pushOutput(fuel.getContainerItem(), true) >= 2) {
//                pushOutput(fuel.getContainerItem(), false);
//                return pushOutput(result, true) >= 1;
//            }
//        }
//        return false;
//    }

    public boolean tryStartBurning(ItemStack fuel, ItemStack ingredient) {
        if (!fuel.isEmpty() && !ingredient.isEmpty()) {
            ItemStack result = FurnaceUtils.getSmeltingResultForItem(level, ingredient);
            if(fuel.hasCraftingRemainingItem()) {
                FurnaceMk2.debugLog("Fuel has a container item to try to push.");
                ItemStack fuelRemainder = fuel.getCraftingRemainingItem();
                if (canPushAllOutputs(new ItemStack[]{result, fuelRemainder})){
                    FurnaceMk2.debugLog("Either the ingedient or the bucket say there's room for two");
                    pushOutput(fuelRemainder, false);
                    //return pushOutput(result, true) >= 1;
                    return true;
                }
            }
            else return pushOutput(result, true) >= 1;
        }
        return false;
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
        double adjustment = getStackInSlot(Area.AUGMENT, 2).isEmpty() ? ConfigSettings.FURNACE_BASE_EXPERIENCE.get() : ConfigSettings.FURNACE_UPGRADED_EXPERIENCE.get()*ConfigSettings.FURNACE_BASE_EXPERIENCE.get();
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

        furnaceData.burnProgress = tag.getInt("burnProgress");
        furnaceData.currentMaxBurn = tag.getInt("currentMaxBun");
        furnaceData.cookProgress = tag.getInt("cookProgress");
        furnaceData.cookMax = tag.getInt("cookMax");
        furnaceData.experience = tag.getInt("experience");
        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.put("invOut", outputHandler.serializeNBT());
        tag.put("invAug", augmentHandler.serializeNBT());
        tag.put("invFuel", fuelHandler.serializeNBT());
        tag.put("invIngr", ingredientHandler.serializeNBT());
        tag.put("invExp", experienceHandler.serializeNBT());

        tag.putInt("burnProgress", furnaceData.burnProgress);
        tag.putInt("currentMaxBun", furnaceData.currentMaxBurn);
        tag.putInt("cookProgress", furnaceData.cookProgress);
        tag.putInt("cookMax", furnaceData.cookMax);
        tag.putInt("experience", furnaceData.experience);
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

    public Component getDisplayName() {
        return Component.translatable("block.furnacemk2.furnacemk2");
    }

    @Nullable
    public AbstractContainerMenu createMenu(int window, Inventory playerInv, Player player) {
        assert this.level != null;
        return new FurnaceMk2Container(window, this.level, this.worldPosition, playerInv, player, this.furnaceData);
    }

    public int getMaxExp() {
        return this.maxExp;
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
