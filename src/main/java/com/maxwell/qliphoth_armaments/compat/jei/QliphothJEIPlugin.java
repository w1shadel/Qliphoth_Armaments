package com.maxwell.qliphoth_armaments.compat.jei;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.recipe.CauldronRecipe;
import com.maxwell.qliphoth_armaments.init.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

@JeiPlugin
public class QliphothJEIPlugin implements IModPlugin {

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(QA.MOD_ID, "jei_plugin");

    public static final RecipeType<CauldronRecipe> CAULDRON =
            RecipeType.create(QA.MOD_ID, "cauldron_brewing", CauldronRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new CauldronRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(Items.CAULDRON),
                CAULDRON
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var recipes = Minecraft.getInstance().level
                .getRecipeManager()
                .getAllRecipesFor(ModRecipes.CAULDRON_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(CAULDRON, recipes);
    }
}