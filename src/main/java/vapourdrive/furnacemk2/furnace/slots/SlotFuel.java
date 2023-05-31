package vapourdrive.furnacemk2.furnace.slots;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class SlotFuel extends AbstractFurnaceMk2Slot {
    private final IItemHandler itemHandler;
    private final int index;

    public SlotFuel(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition, "message.furnacemk2.fuelslot");
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) == 0)
            return false;
        return itemHandler.isItemValid(index, stack);
    }
}
