package vapourdrive.furnacemk2.furnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.config.ConfigSettings;
import vapourdrive.furnacemk2.furnace.itemhandlers.*;
import vapourdrive.furnacemk2.items.IExperienceStorage;
import vapourdrive.furnacemk2.utils.FurnaceUtils;
import vapourdrive.vapourware.shared.base.AbstractBaseFuelUserTile;
import vapourdrive.vapourware.shared.base.itemhandlers.FuelHandler;
import vapourdrive.vapourware.shared.base.itemhandlers.OutputHandler;
import vapourdrive.vapourware.shared.utils.MachineUtils;
import vapourdrive.vapourware.shared.utils.MachineUtils.Area;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static vapourdrive.furnacemk2.setup.Registration.FURNACEMK2_TILE;

public class FurnaceMk2Tile extends AbstractBaseFuelUserTile {

    private final FurnaceAugmentHandler augmentHandler = new FurnaceAugmentHandler(this, AUGMENT_SLOTS.length);
    private final FuelHandler fuelHandler = new FuelHandler(this, FUEL_SLOT.length);
    private final FurnaceIngredientHandler ingredientHandler = new FurnaceIngredientHandler(this, INPUT_SLOT.length);
    private final FurnaceExperienceHandler experienceHandler = new FurnaceExperienceHandler(this, EXPERIENCE_OUTPUT_SLOTS.length);
    private final OutputHandler outputHandler = new OutputHandler(this, OUTPUT_SLOTS.length);
    private final LazyOptional<OutputHandler> lazyOutputHandler = LazyOptional.of(() -> outputHandler);
    private final CombinedInvWrapper combined = new CombinedInvWrapper(augmentHandler, fuelHandler, ingredientHandler, outputHandler, experienceHandler);
    private final LazyOptional<CombinedInvWrapper> combinedHandler = LazyOptional.of(() -> combined);

    private ItemStack lastSmelting = ItemStack.EMPTY;

    public final int maxExp = 50000;
    public int wait = 20;
    private ItemStack currentResult = ItemStack.EMPTY;
    private ItemStack currentIngredient = ItemStack.EMPTY;

    public final FurnaceData furnaceData = new FurnaceData();
    public static final int[] AUGMENT_SLOTS = {0, 1, 2};
    public static final int[] FUEL_SLOT = {0};
    public static final int[] INPUT_SLOT = {0};
    public static final int[] OUTPUT_SLOTS = {0, 1, 2, 3};
    public static final int[] EXPERIENCE_OUTPUT_SLOTS = {0};

    public FurnaceMk2Tile(BlockPos pos, BlockState state) {
        super(FURNACEMK2_TILE.get(), pos, state,15000000, 100, new int[]{0, 1, 2, 3});
    }

    public void tickServer(BlockState state) {
        super.tickServer(state);
        ItemStack ingredient = getStackInSlot(Area.INGREDIENT_1, 0);

        //Reset the cook progress if it's a new item
        if (!lastSmelting.isEmpty() && !ItemStack.isSame(ingredient, lastSmelting)) {
            furnaceData.set(FurnaceData.Data.COOK_PROGRESS, 0);
        }
        //keep track of the current item to check next tick
        lastSmelting = ingredient.copy();

        if(!ingredient.isEmpty()) {
            doCookProcesses(ingredient, state);
        }

        doExperienceItemProcesses();
    }

    private void doCookProcesses(ItemStack ingredient, BlockState state) {
        if (furnaceData.get(FurnaceData.Data.COOK_PROGRESS)==0){
            if(currentResult.isEmpty()) {
                currentResult = FurnaceUtils.getSmeltingResultForItem(level, ingredient);
                currentIngredient = ingredient;
                furnaceData.set(FurnaceData.Data.COOK_MAX, FurnaceUtils.getCookTime(level, ingredient));
            }

            if(MachineUtils.pushOutput(currentResult, true, this) >= 1 && furnaceData.get(FurnaceData.Data.FUEL) >= furnaceData.get(FurnaceData.Data.COOK_MAX)){
                assert level != null;
                level.setBlock(worldPosition, state.setValue(BlockStateProperties.LIT, true), Block.UPDATE_ALL);
                this.setChanged();
                progressCook();
            }
        } else if (furnaceData.get(FurnaceData.Data.COOK_PROGRESS) >= 0) {
            progressCook();
            if (furnaceData.get(FurnaceData.Data.COOK_PROGRESS) >= furnaceData.get(FurnaceData.Data.COOK_MAX)) {
                if (MachineUtils.pushOutput(currentResult, false, this) == -1) {
                    FurnaceMk2.debugLog("experience push");
                    depositExperienceFromStack(ingredient);
                    //ingredientHandler.extractItem(INPUT_SLOT[0], 1, false);
                    removeFromSlot(Area.INGREDIENT_1, 0, 1, false);
                    ItemStack remainingIngredient = getStackInSlot(Area.INGREDIENT_1,0);
                    if(remainingIngredient.isEmpty()){
                        currentIngredient = ItemStack.EMPTY;
                        currentResult = ItemStack.EMPTY;

                    } else if (!ItemStack.isSame(remainingIngredient, currentIngredient)) {
                        currentResult = ItemStack.EMPTY;
                        furnaceData.set(FurnaceData.Data.COOK_MAX, 0);
                    }
                    if(remainingIngredient.isEmpty() || furnaceData.get(FurnaceData.Data.FUEL) < furnaceData.get(FurnaceData.Data.COOK_MAX)) {
                        assert level != null;
                        level.setBlock(worldPosition, state.setValue(BlockStateProperties.LIT, false), Block.UPDATE_ALL);
                        this.setChanged();
                    }
                }
                furnaceData.set(FurnaceData.Data.COOK_PROGRESS, 0);
            }
        }
    }

    private void doExperienceItemProcesses() {
        ItemStack experienceStorage = getStackInSlot(Area.INGREDIENT_2, 0);
        if(!experienceStorage.isEmpty() && experienceStorage.getItem() instanceof IExperienceStorage xpStack) {
            if (furnaceData.get(FurnaceData.Data.EXPERIENCE) >= 100) {
                extractExperience(experienceStorage);
            }
            if (xpStack.getCurrentExperienceStored(experienceStorage) >= xpStack.getMaxExperienceStored(experienceStorage)) {
                FurnaceMk2.debugLog("Pushing output for full experience crystal");
                if (MachineUtils.pushOutput(experienceStorage, false, this) == -1) {
                    removeFromSlot(Area.INGREDIENT_2, 0, 1, false);
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
            furnaceData.set(FurnaceData.Data.EXPERIENCE,furnaceData.get(FurnaceData.Data.EXPERIENCE)-100);
        }
    }

    public void progressCook() {
        double speed = getSpeedMultiplier();
        if (furnaceData.get(FurnaceData.Data.FUEL) > 0) {
            furnaceData.set(FurnaceData.Data.COOK_PROGRESS,furnaceData.get(FurnaceData.Data.COOK_PROGRESS)+(int) (100 * speed));
            furnaceData.set(FurnaceData.Data.FUEL,furnaceData.get(FurnaceData.Data.FUEL)-(int) (100 * speed));
        }
    }

    private void depositExperienceFromStack(ItemStack ingredient) {
        double adjustment = getExperienceMultiplier();
        int toAdd = (int)(FurnaceUtils.getExperience(level, ingredient)*100*adjustment);
        FurnaceMk2.debugLog("adding xp: "+toAdd);
        addExperience(toAdd);
    }

    public void addExperience(int toAdd){
        FurnaceMk2.debugLog("current xp: "+furnaceData.get(FurnaceData.Data.EXPERIENCE));
        furnaceData.set(FurnaceData.Data.EXPERIENCE, Math.min(furnaceData.get(FurnaceData.Data.EXPERIENCE) + toAdd, maxExp));
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        outputHandler.deserializeNBT(tag.getCompound("invOut"));
        augmentHandler.deserializeNBT(tag.getCompound("invAug"));
        fuelHandler.deserializeNBT(tag.getCompound("invFuel"));
        ingredientHandler.deserializeNBT(tag.getCompound("invIngr"));
        experienceHandler.deserializeNBT(tag.getCompound("invExp"));
        furnaceData.set(FurnaceData.Data.COOK_PROGRESS, tag.getInt("cookProgress"));
        furnaceData.set(FurnaceData.Data.COOK_MAX, tag.getInt("cookMax"));
        furnaceData.set(FurnaceData.Data.EXPERIENCE, tag.getInt("experience"));
        wait = tag.getInt("wait");
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("invOut", outputHandler.serializeNBT());
        tag.put("invAug", augmentHandler.serializeNBT());
        tag.put("invFuel", fuelHandler.serializeNBT());
        tag.put("invIngr", ingredientHandler.serializeNBT());
        tag.put("invExp", experienceHandler.serializeNBT());
        tag.putInt("cookProgress", furnaceData.get(FurnaceData.Data.COOK_PROGRESS));
        tag.putInt("cookMax", furnaceData.get(FurnaceData.Data.COOK_MAX));
        tag.putInt("experience", furnaceData.get(FurnaceData.Data.EXPERIENCE));
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
            return combinedHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    public IItemHandler getItemHandler () {
        return combined;
    }


    @Override
    public double getSpeedMultiplier(){
        return getStackInSlot(Area.AUGMENT, 1).isEmpty() ? ConfigSettings.FURNACE_BASE_SPEED.get() : ConfigSettings.FURNACE_UPGRADED_SPEED.get()*ConfigSettings.FURNACE_BASE_SPEED.get();
    }

    @Override
    public double getEfficiencyMultiplier(){
        return getStackInSlot(Area.AUGMENT, 0).isEmpty() ? ConfigSettings.FURNACE_BASE_EFFICIENCY.get() : ConfigSettings.FURNACE_UPGRADED_EFFICIENCY.get()*ConfigSettings.FURNACE_BASE_EFFICIENCY.get();
    }

    public double getExperienceMultiplier(){
        return getStackInSlot(MachineUtils.Area.AUGMENT, 2).isEmpty() ? ConfigSettings.FURNACE_BASE_EXPERIENCE.get() : ConfigSettings.FURNACE_UPGRADED_EXPERIENCE.get()*ConfigSettings.FURNACE_BASE_EXPERIENCE.get();
    }

    public int getMaxExp() {
        return this.maxExp;
    }

    @Override
    public int getCurrentFuel() {
        return furnaceData.get(FurnaceData.Data.FUEL);
    }

    @Override
    public boolean addFuel(int toAdd, boolean simulate) {
        if (toAdd + getCurrentFuel() > getMaxFuel()) {
            return false;
        }
        if (!simulate) {
            furnaceData.set(FurnaceData.Data.FUEL, getCurrentFuel() + toAdd);
        }

        return true;
    }

    @Override
    public boolean consumeFuel(int toConsume, boolean simulate) {
        if (getCurrentFuel() < toConsume) {
            return false;
        }
        if (!simulate) {
            furnaceData.set(FurnaceData.Data.FUEL, getCurrentFuel() - toConsume);
        }
        return true;
    }

    public FurnaceData getFurnaceData() {
        return furnaceData;
    }

    public ItemStack getStackInSlot(Area area, int index) {
        return switch(area){
            case AUGMENT -> augmentHandler.getStackInSlot(AUGMENT_SLOTS[index]);
            case INGREDIENT_1 -> ingredientHandler.getStackInSlot(INPUT_SLOT[index]);
            case INGREDIENT_2 -> experienceHandler.getStackInSlot(EXPERIENCE_OUTPUT_SLOTS[index]);
            case FUEL -> fuelHandler.getStackInSlot(FUEL_SLOT[index]);
            case OUTPUT -> outputHandler.getStackInSlot(OUTPUT_SLOTS[index]);
            default -> ItemStack.EMPTY;
        };
    }

    public void removeFromSlot(Area area, int index, int amount, boolean simulate) {
        switch (area) {
            case AUGMENT -> augmentHandler.extractItem(AUGMENT_SLOTS[index], amount, simulate);
            case INGREDIENT_1 -> ingredientHandler.extractItem(INPUT_SLOT[index], amount, simulate);
            case INGREDIENT_2 -> experienceHandler.extractItem(EXPERIENCE_OUTPUT_SLOTS[index], amount, simulate);
            case FUEL -> fuelHandler.extractItem(FUEL_SLOT[index], amount, simulate);
            case OUTPUT -> outputHandler.extractItem(OUTPUT_SLOTS[index], amount, simulate);
        }
    }

    public ItemStack insertToSlot(Area area, int index, ItemStack stack, boolean simulate) {
        return switch(area){
            case AUGMENT -> augmentHandler.insertItem(AUGMENT_SLOTS[index], stack, simulate);
            case INGREDIENT_1 -> ingredientHandler.insertItem(INPUT_SLOT[index], stack, simulate);
            case INGREDIENT_2 -> experienceHandler.insertItem(EXPERIENCE_OUTPUT_SLOTS[index], stack, simulate);
            case FUEL -> fuelHandler.insertItem(FUEL_SLOT[index], stack, simulate);
            case OUTPUT -> outputHandler.insertItem(OUTPUT_SLOTS[index], stack, simulate, true);
            default -> ItemStack.EMPTY;
        };
    }
}
