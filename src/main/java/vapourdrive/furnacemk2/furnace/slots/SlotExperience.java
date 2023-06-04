package vapourdrive.furnacemk2.furnace.slots;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.items.IExperienceStorage;
import vapourdrive.vapourware.shared.base.slots.BaseSlotIngredient;
import vapourdrive.vapourware.shared.utils.DeferredComponent;

public class SlotExperience extends BaseSlotIngredient {
    public SlotExperience(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition, new DeferredComponent(FurnaceMk2.MODID, "experienceslot"));
    }

    @Override
    protected boolean isValidIngredient(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IExperienceStorage;
    }
}
