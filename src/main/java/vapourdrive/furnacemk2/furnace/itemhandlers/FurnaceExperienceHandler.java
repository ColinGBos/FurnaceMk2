package vapourdrive.furnacemk2.furnace.itemhandlers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import vapourdrive.furnacemk2.furnace.FurnaceMk2Tile;
import vapourdrive.furnacemk2.items.IExperienceStorage;
import vapourdrive.furnacemk2.setup.Registration;

import javax.annotation.Nonnull;

public class FurnaceExperienceHandler extends ItemStackHandler {
    FurnaceMk2Tile tile;

    public FurnaceExperienceHandler(FurnaceMk2Tile tile, int size) {
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
        return stack.getItem() instanceof IExperienceStorage;
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
