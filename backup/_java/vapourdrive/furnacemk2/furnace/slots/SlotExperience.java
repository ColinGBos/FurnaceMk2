package vapourdrive.furnacemk2.furnace.slots;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import vapourdrive.furnacemk2.items.IExperienceStorage;

import javax.annotation.Nonnull;

public class SlotExperience extends AbstractFurnaceMk2Slot {
    public SlotExperience(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition, "message.furnacemk2.experienceslot");
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IExperienceStorage;
    }
}
