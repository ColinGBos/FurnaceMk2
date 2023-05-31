package vapourdrive.furnacemk2.furnace.slots;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import vapourdrive.furnacemk2.FurnaceMk2;

import javax.annotation.Nonnull;

public class SlotOutput extends AbstractFurnaceMk2Slot {

    public SlotOutput(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition, null);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return FurnaceMk2.debugMode;
    }
}