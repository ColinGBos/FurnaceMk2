package vapourdrive.furnacemk2.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.furnace.FurnaceMk2Screen;
import vapourdrive.furnacemk2.setup.Registration;

@JeiPlugin
public class JEI_plugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(FurnaceMk2.MODID, "jei_plugin");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        FurnaceMk2.debugLog("Adding Recipe Click Area");
        registration.addRecipeClickArea(FurnaceMk2Screen.class, 72, 38, 16, 15, RecipeTypes.FUELING, RecipeTypes.SMELTING);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        FurnaceMk2.debugLog("Register recipe catalyst");
        registration.addRecipeCatalyst(new ItemStack(Registration.FURNACEMK2_BLOCK.get()), RecipeTypes.FUELING, RecipeTypes.SMELTING);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(new ItemStack(Registration.FURNACEMK2_ITEM.get()), VanillaTypes.ITEM_STACK, Component.translatable("furnacemk2.furnacemk2.info"));
    }
}
