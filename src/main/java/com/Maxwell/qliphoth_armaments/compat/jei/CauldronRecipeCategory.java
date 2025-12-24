package com.maxwell.qliphoth_armaments.compat.jei;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.recipe.CauldronRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CauldronRecipeCategory implements IRecipeCategory<CauldronRecipe> {

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;
    private final IDrawable plus;
    private final Component localizedName;
    private static final ResourceLocation JEI_GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("jei", "textures/jei/gui/gui_vanilla.png");
    private static final ResourceLocation PLUS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(QA.MOD_ID, "textures/gui/plus.png");
    public CauldronRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(124, 40);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.CAULDRON));
        this.localizedName = Component.translatable("jei.qliphoth_armaments.category.cauldron_brewing");
        this.plus = guiHelper.createDrawable(
                PLUS_TEXTURE,
                0, 0,
                13, 13    
        );
        IDrawableStatic arrowStatic = guiHelper.createDrawable(
                JEI_GUI_TEXTURE,
                82, 128, 
                24, 17
        );
        this.arrow = guiHelper.createAnimatedDrawable(
                arrowStatic,
                200,
                IDrawableAnimated.StartDirection.LEFT,
                false
        );
    }

    @Override
    public RecipeType<CauldronRecipe> getRecipeType() {
        return QliphothJEIPlugin.CAULDRON;
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
        if (Minecraft.getInstance().level != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 12)
                    .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
        }
    }

    @Override
    public void draw(CauldronRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 62, 12);
        plus.draw(guiGraphics, 27, 13);
    }
}