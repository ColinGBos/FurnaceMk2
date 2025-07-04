package vapourdrive.furnacemk2.utils;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class FurnaceUtils {
    public static boolean canSmelt(ItemStack stack, Level world) {
        return world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), world).isPresent();
    }

    public static float getExperience(Level world, ItemStack itemStack) {
        Optional<RecipeHolder<SmeltingRecipe>> matchingRecipe = getMatchingRecipeForInput(world, itemStack);
        return matchingRecipe.map(holder -> holder.value().getExperience()).orElse(0f);

    }

    public static int getCookTime(Level world, ItemStack itemStack) {
        Optional<RecipeHolder<SmeltingRecipe>> matchingRecipe = getMatchingRecipeForInput(world, itemStack);
//        return matchingRecipe.map(AbstractCookingRecipe::getCookingTime).orElse(200) * 100;
        return matchingRecipe.map(holder -> holder.value().getCookingTime()).orElse(200) * 100;
    }

    public static ItemStack getSmeltingResultForItem(Level world, ItemStack itemStack) {
        Optional<RecipeHolder<SmeltingRecipe>> matchingRecipe = getMatchingRecipeForInput(world, itemStack);
        return matchingRecipe.map(holder -> holder.value().getResultItem(world.registryAccess()).copy()).orElse(ItemStack.EMPTY);
    }

    public static Optional<RecipeHolder<SmeltingRecipe>> getMatchingRecipeForInput(Level world, ItemStack itemStack) {
        RecipeManager recipeManager = world.getRecipeManager();
        SingleRecipeInput singleItemInventory = new SingleRecipeInput(itemStack);
        return recipeManager.getRecipeFor(RecipeType.SMELTING, singleItemInventory, world);
    }

}
