package vapourdrive.furnacemk2.furnace.itemhandlers;

import net.minecraft.world.item.ItemStack;
import vapourdrive.furnacemk2.furnace.FurnaceMk2Tile;
import vapourdrive.furnacemk2.items.IExperienceStorage;
import vapourdrive.vapourware.shared.base.itemhandlers.IngredientHandler;

import javax.annotation.Nonnull;

public class FurnaceExperienceHandler extends IngredientHandler {

    public FurnaceExperienceHandler(FurnaceMk2Tile tile, int size) {
        super(tile, size);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.getItem() instanceof IExperienceStorage;
    }
}
