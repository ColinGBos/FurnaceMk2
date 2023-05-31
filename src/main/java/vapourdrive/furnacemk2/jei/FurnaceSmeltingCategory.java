package vapourdrive.furnacemk2.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.NotNull;
import vapourdrive.furnacemk2.FurnaceMk2;

public class FurnaceSmeltingCategory implements IRecipeCategory<SmeltingRecipe> {

    @Override
    public @NotNull RecipeType<SmeltingRecipe> getRecipeType() {
        return RecipeTypes.SMELTING;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable(FurnaceMk2.MODID + ".jei_category_regular");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return null;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return null;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SmeltingRecipe recipe, IFocusGroup focuses) {

    }
}
