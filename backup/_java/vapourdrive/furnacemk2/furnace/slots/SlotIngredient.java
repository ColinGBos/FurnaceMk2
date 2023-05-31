package vapourdrive.furnacemk2.furnace.slots;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class SlotIngredient extends AbstractFurnaceMk2Slot {
    private final IItemHandler itemHandler;
    private final int index;
    protected final Level world;

    public SlotIngredient(IItemHandler itemHandler, int index, int xPosition, int yPosition, Level world) {
        super(itemHandler, index, xPosition, yPosition, "message.furnacemk2.ingredientslot");
        this.itemHandler = itemHandler;
        this.index = index;
        this.world = world;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || !this.canSmelt(stack))
            return false;
        return itemHandler.isItemValid(index, stack);
    }


    protected boolean canSmelt(ItemStack stack) {
        return this.world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), this.world).isPresent();
    }
}
