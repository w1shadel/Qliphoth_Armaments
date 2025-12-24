package com.maxwell.qliphoth_armaments.common.recipe;

import com.maxwell.qliphoth_armaments.init.ModRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CauldronRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient input;
    private final ItemStack output;

    public static final MapCodec<CauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CauldronRecipe::getInput),
            ItemStack.CODEC.fieldOf("result").forGetter(r -> r.output)
    ).apply(inst, CauldronRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CauldronRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CauldronRecipe::getInput,
            ItemStack.STREAM_CODEC, r -> r.output,
            CauldronRecipe::new
    );

    public CauldronRecipe(Ingredient input, ItemStack output) {
        this.input = input;
        this.output = output;
    }

    public Ingredient getInput() {
        return input;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {

        return this.input.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CAULDRON_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CAULDRON_TYPE.get();
    }
    public static class Serializer implements RecipeSerializer<CauldronRecipe> {
        @Override
        public MapCodec<CauldronRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CauldronRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}