package vapourdrive.furnacemk2.furnace.slots;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import vapourdrive.furnacemk2.items.IExperienceStorage;
import vapourdrive.vapourware.shared.base.slots.BaseSlotIngredient;

public class SlotExperience extends BaseSlotIngredient {
    public SlotExperience(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition, "furnacemk2.experienceslot");
    }

    @Override
    protected boolean isValidIngredient(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IExperienceStorage;
    }
}
