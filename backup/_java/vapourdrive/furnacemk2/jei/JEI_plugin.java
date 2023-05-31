package vapourdrive.furnacemk2.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import mezz.jei.api.constants.RecipeTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.resources.ResourceLocation;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.furnace.FurnaceMk2Container;
import vapourdrive.furnacemk2.furnace.FurnaceMk2Screen;
import vapourdrive.furnacemk2.setup.Registration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JeiPlugin
public class JEI_plugin implements IModPlugin {

    @Nullable
    private IRecipeCategory<SmeltingRecipe> furnaceCategory;

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(FurnaceMk2.MODID, "jei_plugin");
    }

//    @Override
//    public void registerCategories(IRecipeCategoryRegistration registration) {
//        FurnaceMk2.debugLog("Registering Furnace Category");
//        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
//        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
//        registration.addRecipeCategories(
//            furnaceCategory = new FurnaceSmeltingCategory(guiHelper)
//        );
//    }

//    @Override
//    public void registerRecipes(IRecipeRegistration registration) {
//        FurnaceMk2.debugLog("Registering Furnace Recipe");
//        registration.addRecipes(RecipeTypes.SMELTING, RecipeTypes.SMELTING);
//    }


    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        FurnaceMk2.debugLog("Adding Recipe Click Area");
        registration.addRecipeClickArea(FurnaceMk2Screen.class, 68, 38, 16, 15, RecipeTypes.FUELING, RecipeTypes.SMELTING);
    }

//    @Override
//    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
//        FurnaceMk2.debugLog("Register Recipe Transfer Handler");
//        registration.addRecipeTransferHandler(FurnaceMk2Container.class, RecipeTypes.FUELING, 39, 1, 0, 45);
//        registration.addRecipeTransferHandler(FurnaceMk2Container.class, RecipeTypes.SMELTING, 40, 1, 0, 45);
//    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        FurnaceMk2.debugLog("Register recipe catalyst");
        registration.addRecipeCatalyst(new ItemStack(Registration.FURNACEMK2_BLOCK.get()), RecipeTypes.FUELING, RecipeTypes.SMELTING);
    }
}
