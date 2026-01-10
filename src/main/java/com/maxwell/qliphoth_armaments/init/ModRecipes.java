package com.maxwell.qliphoth_armaments.init;

import com.maxwell.qliphoth_armaments.common.recipe.CauldronRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, "qliphoth_armaments");

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, "qliphoth_armaments");

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CauldronRecipe>> CAULDRON_SERIALIZER =
            SERIALIZERS.register("cauldron_brewing", CauldronRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CauldronRecipe>> CAULDRON_TYPE =
            TYPES.register("cauldron_brewing", () -> new RecipeType<CauldronRecipe>() {
                @Override
                public String toString() {
                    return "qliphoth_armaments:cauldron_brewing";
                }
            });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}