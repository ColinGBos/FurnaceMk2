package vapourdrive.furnacemk2.furnace.slots;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AbstractFurnaceMk2Slot extends SlotItemHandler {
    public final String slotTitle;
    public AbstractFurnaceMk2Slot(IItemHandler itemHandler, int index, int xPosition, int yPosition, String slotTitle) {
        super(itemHandler, index, xPosition, yPosition);
        this.slotTitle = slotTitle;
    }

    public String getTitle() {
        return this.slotTitle;
    }
}
