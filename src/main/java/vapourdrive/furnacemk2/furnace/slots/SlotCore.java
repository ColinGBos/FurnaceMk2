package vapourdrive.furnacemk2.furnace.slots;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.vapourware.shared.base.slots.BaseSlotIngredient;
import vapourdrive.vapourware.shared.utils.DeferredComponent;

public class SlotCore extends BaseSlotIngredient {
    private final Item upgradeItem;

    public SlotCore(IItemHandler itemHandler, int index, int xPosition, int yPosition, Item item) {
        super(itemHandler, index, xPosition, yPosition, new DeferredComponent(FurnaceMk2.MODID, "upgradeslot", item.getDescription()));
        this.upgradeItem = item;
    }

    @Override
    protected boolean isValidIngredient(ItemStack stack) {
        return stack.is(upgradeItem);
    }

}
