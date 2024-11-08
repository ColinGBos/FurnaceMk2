package vapourdrive.furnacemk2.furnace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.furnace.slots.SlotCore;
import vapourdrive.furnacemk2.furnace.slots.SlotExperience;
import vapourdrive.furnacemk2.furnace.slots.SlotIngredient;
import vapourdrive.furnacemk2.items.IExperienceStorage;
import vapourdrive.furnacemk2.setup.Registration;
import vapourdrive.furnacemk2.utils.FurnaceUtils;
import vapourdrive.vapourware.shared.base.AbstractBaseMachineMenu;
import vapourdrive.vapourware.shared.base.slots.SlotFuel;
import vapourdrive.vapourware.shared.base.slots.SlotOutput;

import java.util.Objects;

public class FurnaceMk2Menu extends AbstractBaseMachineMenu {

    // gui position of the player inventory grid
    public static final int PLAYER_INVENTORY_XPOS = 8;
    public static final int PLAYER_INVENTORY_YPOS = 84;

    protected final FurnaceMk2Tile tileEntity;

    public FurnaceMk2Menu(int windowId, Level world, BlockPos pos, Inventory inv, Player player, FurnaceData furnaceData) {
        super(windowId, world, pos, inv, player, Registration.FURNACEMK2_MENU.get(),furnaceData);
        tileEntity = (FurnaceMk2Tile) world.getBlockEntity(pos);

        //We use this vs the builtin method because we split all the shorts
        addSplitDataSlots(furnaceData);

        layoutPlayerInventorySlots(PLAYER_INVENTORY_XPOS, PLAYER_INVENTORY_YPOS);

        if (tileEntity != null && tileEntity instanceof FurnaceMk2Tile machine) {
            IItemHandler handler = machine.getItemHandler(null);
            addSlot(new SlotCore(handler, 0, 8, 17, Registration.INSULATION_CORE_ITEM.get()));
            addSlot(new SlotCore(handler, 1, 8, 35, Registration.THERMAL_CORE_ITEM.get()));
            addSlot(new SlotCore(handler, 2, 8, 53, Registration.EXPERIENCE_CORE_ITEM.get()));
            addSlot(new SlotFuel(handler, 3, 49, 53));
            addSlot(new SlotIngredient(handler, 4, 49, 22, this.world));
            addSlot(new SlotOutput(handler, 5, 94, 22));
            addSlot(new SlotOutput(handler, 6, 112, 22));
            addSlot(new SlotOutput(handler, 7, 130, 22));
            addSlot(new SlotOutput(handler, 8, 148, 22));
            addSlot(new SlotExperience(handler, 9, 148, 53));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn) {
        return stillValid(ContainerLevelAccess.create(Objects.requireNonNull(tileEntity.getLevel()), tileEntity.getBlockPos()), playerEntity, Registration.FURNACEMK2_BLOCK.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        FurnaceMk2.debugLog("index: " +index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();

            //Furnace outputs to Inventory
            if (index >= 41 && index <= 45) {
                FurnaceMk2.debugLog("From furnace output");
                if (!this.moveItemStackTo(stack, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            }

            //Non-output slots to Inventory
            if (index >= 36 && index <= 40) {
                FurnaceMk2.debugLog("From furnace non-output");
                if (!this.moveItemStackTo(stack, 0, 36, false)) {
                    return ItemStack.EMPTY;
                }
            }

            //Player Inventory
            else if (index <= 35) {
                //Inventory to fuel
                if (stack.getBurnTime(RecipeType.SMELTING) > 0.0) {
                    if (!this.moveItemStackTo(stack, 39, 40, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                //Inventory to smelt slot
                if (FurnaceUtils.canSmelt(stack, this.world)) {
                    FurnaceMk2.debugLog("From Player inventory to smelt slot");
                    if (!this.moveItemStackTo(stack, 40, 41, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                //Inventory to augment
                if (stack.getItem() == Registration.INSULATION_CORE_ITEM.get() || stack.getItem() == Registration.THERMAL_CORE_ITEM.get() || stack.getItem() == Registration.EXPERIENCE_CORE_ITEM.get()) {
                    if (!this.moveItemStackTo(stack, 36, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                //Inventory to experience slot
                if (stack.getItem() instanceof IExperienceStorage) {
                    FurnaceMk2.debugLog("From Player inventory to experience slot");
                    if (!this.moveItemStackTo(stack, 45, 46, false)) {
                        return ItemStack.EMPTY;
                    }
                }

                //Inventory to hotbar
                if (index <= 26) {
                    FurnaceMk2.debugLog("From Player inventory to hotbar");
                    if (!this.moveItemStackTo(stack, 27, 36, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                //Hotbar to inventory
                else {
                    FurnaceMk2.debugLog("From Hotbar to inventory");
                    if (!this.moveItemStackTo(stack, 0, 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stack);
        }

        return itemstack;
    }

    @OnlyIn(Dist.CLIENT)
    public float getCookProgress() {
        int i = this.machineData.get(FurnaceData.Data.COOK_PROGRESS.ordinal());
        if (i == 0) {
            return 0;
        }
        return (float) i / (float) this.machineData.get(FurnaceData.Data.COOK_MAX.ordinal());
    }

    @OnlyIn(Dist.CLIENT)
    public float getExperiencePercentage() {
        int i = this.machineData.get(FurnaceData.Data.EXPERIENCE.ordinal());
        if (i == 0) {
            return 0;
        }
        return (float) i / (float) FurnaceMk2Tile.getMaxExp();
    }

    @OnlyIn(Dist.CLIENT)
    public int getExperienceStored() {
        return this.machineData.get(FurnaceData.Data.EXPERIENCE.ordinal());
    }

    public int getMaxExp() {
        return FurnaceMk2Tile.getMaxExp();
    }
}
