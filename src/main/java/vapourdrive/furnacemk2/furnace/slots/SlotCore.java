package vapourdrive.furnacemk2.furnace.slots;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import vapourdrive.vapourware.shared.base.slots.BaseSlotIngredient;

public class SlotCore extends BaseSlotIngredient {
    private final Item upgradeItem;

    public SlotCore(IItemHandler itemHandler, int index, int xPosition, int yPosition, Item item) {
        super(itemHandler, index, xPosition, yPosition, "furnacemk2.upgradeslot");
        this.upgradeItem = item;
    }

    @Override
    protected boolean isValidIngredient(ItemStack stack) {
        return stack.isEmpty() || stack.getItem() != upgradeItem;
    }


    public Item getUpgradeItem() {
        return this.upgradeItem;
    }

    @Override
    public String getTitle() {
        return this.slotTitle+ ": "+ Component.translatable(getUpgradeItem().getDescriptionId());
    }
}
