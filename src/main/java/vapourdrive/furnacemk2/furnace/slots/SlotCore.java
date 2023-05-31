package vapourdrive.furnacemk2.furnace.slots;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class SlotCore extends AbstractFurnaceMk2Slot {
    private final IItemHandler itemHandler;
    private final int index;
    private final Item upgradeItem;

    public SlotCore(IItemHandler itemHandler, int index, int xPosition, int yPosition, Item item) {
        super(itemHandler, index, xPosition, yPosition, "message.furnacemk2.upgradeslot");
        this.itemHandler = itemHandler;
        this.index = index;
        this.upgradeItem = item;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != upgradeItem)
            return false;
        return itemHandler.isItemValid(index, stack);
    }

    public Item getUpgradeItem() {
        return this.upgradeItem;
    }
}
