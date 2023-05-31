package vapourdrive.furnacemk2.furnace;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.furnace.slots.*;
import vapourdrive.furnacemk2.items.IExperienceStorage;
import vapourdrive.furnacemk2.setup.Registration;
import vapourdrive.furnacemk2.utils.FurnaceUtils;

public class FurnaceMk2Container extends AbstractContainerMenu {

    private final FurnaceMk2Tile tileEntity;
    private final Player playerEntity;
    private final IItemHandler playerInventory;
    protected final Level world;
    private final FurnaceData furnaceData;

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;

    // slot index is the unique index for all slots in this container i.e. 0 - 35 for invPlayer then 36 - 45 for furnaceContents
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int HOTBAR_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX;
    private static final int PLAYER_INVENTORY_FIRST_SLOT_INDEX = HOTBAR_FIRST_SLOT_INDEX + HOTBAR_SLOT_COUNT;
    private static final int FIRST_FUEL_SLOT_INDEX = PLAYER_INVENTORY_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT;

    // gui position of the player inventory grid
    public static final int PLAYER_INVENTORY_XPOS = 8;
    public static final int PLAYER_INVENTORY_YPOS = 84;

//    public FurnaceMk2Container(int windowId, Level world, BlockPos pos, Inventory inv, Player player) {
//        this(windowId, world, pos, inv, player, new FurnaceData());
//    }

    public FurnaceMk2Container(int windowId, Level world, BlockPos pos, Inventory inv, Player player, FurnaceData furnaceData) {
        super(Registration.FURNACEMK2_CONTAINER.get(),windowId);
        tileEntity = (FurnaceMk2Tile) world.getBlockEntity(pos);
        this.playerEntity = player;
        this.playerInventory = new InvWrapper(inv);
        this.world = world;
        this.furnaceData = furnaceData;

        //We use this vs the built in method because we split all the shorts
        addSplitDataSlots(furnaceData);

        layoutPlayerInventorySlots(PLAYER_INVENTORY_XPOS, PLAYER_INVENTORY_YPOS);

        if (tileEntity != null) {
            tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(h -> {
                addSlot(new SlotCore(h, 0, 16, 17, Registration.INSULATION_CORE_ITEM.get()));
                addSlot(new SlotCore(h, 1, 16, 35, Registration.THERMAL_CORE_ITEM.get()));
                addSlot(new SlotCore(h, 2, 16, 53, Registration.EXPERIENCE_CORE_ITEM.get()));
                addSlot(new SlotFuel(h, 3, 45, 53));
                addSlot(new SlotIngredient(h, 4, 45, 17, this.world));
                addSlot(new SlotOutput(h, 5, 90, 17));
                addSlot(new SlotOutput(h, 6, 108, 17));
                addSlot(new SlotOutput(h, 7, 126, 17));
                addSlot(new SlotOutput(h, 8, 144, 17));
                addSlot(new SlotExperience(h, 9, 144, 53));
            });
        }
    }

    //Full disclosure, I don't really know how tf to do bit 'stuff' but it seems to work
    protected void addSplitDataSlots(ContainerData data) {
        for(int i = 0; i < data.getCount(); ++i) {
            int index = i;
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return data.get(index) & 0xffff;
                }

                @Override
                public void set(int value) {
                    int stored = data.get(index) & 0xffff0000;
                    data.set(index, stored + (value & 0xffff));
                }
            });
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return (data.get(index) >> 16) & 0xffff;
                }

                @Override
                public void set(int value) {
                    int stored = data.get(index) & 0x0000ffff;
                    data.set(index, stored | value << 16);
                }
            });
        }

    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos()), playerEntity, Registration.FURNACEMK2_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        FurnaceMk2.debugLog("index: " +index);

        if (slot != null && slot.hasItem()) {
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
                if (ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0.0) {
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

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int columns, int spacingX, int rows, int spacingY) {
        for (int j = 0 ; j < rows ; j++) {
            index = addSlotRange(handler, index, x, y, columns, spacingX);
            y += spacingY;
        }
        return index;
    }

    private int layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        int index = addSlotRange(playerInventory, 0, leftCol, topRow+58, 9, 18);

        //hotbar
        index = addSlotBox(playerInventory, index, leftCol, topRow, 9, 18, 3, 18);

        return index;

    }

    public int getFurnaceData(int request) {
        return this.furnaceData.get(request);
    }

    @OnlyIn(Dist.CLIENT)
    public float getBurnProgress() {
        int maxBurn = this.furnaceData.get(1);
        int currentBurn = this.furnaceData.get(0);
        return maxBurn != 0 && currentBurn != 0 ? (float) currentBurn / (float) maxBurn : 0;
    }

    @OnlyIn(Dist.CLIENT)
    public float getCookProgress() {
        int i = this.furnaceData.get(2);
        if (i == 0) {
            return 0;
        }
        return (float) i / (float) this.furnaceData.get(4);
    }

    @OnlyIn(Dist.CLIENT)
    public float getExperiencePercentage() {
        int i = this.furnaceData.get(3);
        if (i == 0) {
            return 0;
        }
        return (float) i / (float) tileEntity.getMaxExp();
    }

    @OnlyIn(Dist.CLIENT)
    public int getExperienceStored() {
        return this.furnaceData.get(3);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isLit() {
        return this.furnaceData.get(0) > 0;
    }

    public int getMaxExp() {
        return tileEntity.getMaxExp();
    }
}
