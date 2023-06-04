package vapourdrive.furnacemk2.furnace.itemhandlers;

import net.minecraft.world.item.ItemStack;
import vapourdrive.furnacemk2.furnace.FurnaceMk2Tile;
import vapourdrive.furnacemk2.setup.Registration;
import vapourdrive.vapourware.shared.base.itemhandlers.IngredientHandler;

import javax.annotation.Nonnull;

public class FurnaceAugmentHandler extends IngredientHandler {

    public FurnaceAugmentHandler(FurnaceMk2Tile tile, int size) {
        super(tile, size);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.getItem() == Registration.INSULATION_CORE_ITEM.get() || stack.getItem() == Registration.THERMAL_CORE_ITEM.get() || stack.getItem() == Registration.EXPERIENCE_CORE_ITEM.get();
    }
}
