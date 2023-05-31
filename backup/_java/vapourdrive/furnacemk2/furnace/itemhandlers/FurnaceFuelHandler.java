package vapourdrive.furnacemk2.furnace.itemhandlers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemStackHandler;
import vapourdrive.furnacemk2.furnace.FurnaceMk2Tile;

import javax.annotation.Nonnull;

public class FurnaceFuelHandler extends ItemStackHandler {
    FurnaceMk2Tile tile;

    public FurnaceFuelHandler(FurnaceMk2Tile tile, int size) {
        this.tile = tile;
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    protected void onContentsChanged(int slot) {
        // To make sure the TE persists when the chunk is saved later we need to
        // mark it dirty every time the item handler changes
        tile.setChanged();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0.0;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return super.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return super.extractItem(slot, amount, simulate);
    }
}
