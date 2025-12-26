package com.maxwell.qliphoth_armaments.compat.jei;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.recipe.CauldronRecipe;
import com.maxwell.qliphoth_armaments.init.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(QA.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CauldronRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
        List<CauldronRecipe> cauldronRecipes = recipeManager.getAllRecipesFor(ModRecipes.CAULDRON_TYPE.get());
        registration.addRecipes(CauldronRecipeCategory.RECIPE_TYPE, cauldronRecipes);
    }
}