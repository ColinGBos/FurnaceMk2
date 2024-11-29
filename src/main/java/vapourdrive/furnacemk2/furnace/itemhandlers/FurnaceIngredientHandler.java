package vapourdrive.furnacemk2.furnace.itemhandlers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.furnace.FurnaceMk2Tile;
import vapourdrive.furnacemk2.utils.FurnaceUtils;
import vapourdrive.vapourware.shared.base.itemhandlers.IngredientHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class FurnaceIngredientHandler extends IngredientHandler {
    public FurnaceIngredientHandler(FurnaceMk2Tile tile, int size) {
        super(tile, size);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        Level world = tile.getLevel();
        assert world != null;
        return FurnaceUtils.canSmelt(stack, world);
    }

    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
//        FurnaceMk2.debugLog("slot count: "+getStackInSlot(slot).getCount()+", amount: "+amount);
        if (getStackInSlot(slot).getCount() == amount){
            FurnaceMk2.debugLog("slot count: "+getStackInSlot(slot).getCount()+", amount: "+amount);
            Objects.requireNonNull(tile.getLevel()).setBlock(tile.getBlockPos(), tile.getBlockState().setValue(BlockStateProperties.LIT, false), Block.UPDATE_ALL);
        }
        return super.extractItem(slot, amount, simulate);
    }
}
