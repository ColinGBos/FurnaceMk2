package vapourdrive.furnacemk2.furnace.slots;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import vapourdrive.vapourware.shared.base.slots.BaseSlotIngredient;

public class SlotIngredient extends BaseSlotIngredient {

    protected final Level world;

    public SlotIngredient(IItemHandler itemHandler, int index, int xPosition, int yPosition, Level world) {
        super(itemHandler, index, xPosition, yPosition);
        this.world = world;
    }

    @Override
    protected boolean isValidIngredient(ItemStack stack) {
        return this.world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), this.world).isPresent();
    }
}
