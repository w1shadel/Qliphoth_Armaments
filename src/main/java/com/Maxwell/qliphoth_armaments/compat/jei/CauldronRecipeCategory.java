package com.Maxwell.qliphoth_armaments.compat.jei;

import com.Maxwell.qliphoth_armaments.QA;
import com.Maxwell.qliphoth_armaments.common.recipe.CauldronRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CauldronRecipeCategory implements IRecipeCategory<CauldronRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(QA.MOD_ID, "cauldron_brewing");
    public static final RecipeType<CauldronRecipe> RECIPE_TYPE = new RecipeType<>(UID, CauldronRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;
    private final IDrawable plus;
    private final Component localizedName;

    public CauldronRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(124, 40);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.CAULDRON));
        this.localizedName = Component.translatable("jei.qliphoth_armaments.category.cauldron_brewing");
        this.plus = guiHelper.getRecipePlusSign();
        this.arrow = guiHelper.createAnimatedRecipeArrow(200);
    }

    @Override
    public RecipeType<CauldronRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CauldronRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 12)
                .addIngredients(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.INPUT, 43, 12)
                .addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.WATER_BUCKET));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 12)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(CauldronRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        plus.draw(guiGraphics, 28, 14);
        arrow.draw(guiGraphics, 62, 12);
    }
}