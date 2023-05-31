package vapourdrive.furnacemk2.utils;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class FurnaceUtils {
    public static boolean canSmelt(ItemStack stack, Level world) {
        return world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), world).isPresent();
    }

    public static float getExperience(Level world, ItemStack itemStack) {
        Optional<SmeltingRecipe> matchingRecipe = getMatchingRecipeForInput(world, itemStack);
        return matchingRecipe.map(AbstractCookingRecipe::getExperience).orElse(0f);
    }

    public static int getCookTime(Level world, ItemStack itemStack) {
        Optional<SmeltingRecipe> matchingRecipe = getMatchingRecipeForInput(world, itemStack);
        return matchingRecipe.map(AbstractCookingRecipe::getCookingTime).orElse(200) * 100;
    }

    public static ItemStack getSmeltingResultForItem(Level world, ItemStack itemStack) {
        Optional<SmeltingRecipe> matchingRecipe = getMatchingRecipeForInput(world, itemStack);
        return matchingRecipe.map(furnaceRecipe -> furnaceRecipe.getResultItem().copy()).orElse(ItemStack.EMPTY);
    }

    public static Optional<SmeltingRecipe> getMatchingRecipeForInput(Level world, ItemStack itemStack) {
        RecipeManager recipeManager = world.getRecipeManager();
        SimpleContainer singleItemInventory = new SimpleContainer(itemStack);
        return recipeManager.getRecipeFor(RecipeType.SMELTING, singleItemInventory, world);
    }

}
