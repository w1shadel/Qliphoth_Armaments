package com.Maxwell.qliphoth_armaments.init;

import com.Maxwell.qliphoth_armaments.common.recipe.CauldronRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "qliphoth_armaments");

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, "qliphoth_armaments");

    public static final RegistryObject<RecipeSerializer<CauldronRecipe>> CAULDRON_SERIALIZER =
            SERIALIZERS.register("cauldron_cooling", CauldronRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<CauldronRecipe>> CAULDRON_TYPE =
            TYPES.register("cauldron_cooling", () -> new RecipeType<CauldronRecipe>() {
                @Override
                public String toString() {
                    return "qliphoth_armaments:cauldron_cooling";
                }
            });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}