package com.maxwell.qliphoth_armaments.common.event;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.entity.ChesedCoreMinionEntity;
import com.maxwell.qliphoth_armaments.common.recipe.CauldronRecipe;
import com.maxwell.qliphoth_armaments.init.ModItems;
import com.maxwell.qliphoth_armaments.init.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = QA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewTarget();
        if (newTarget instanceof ChesedCoreMinionEntity minion) {
            Player owner = minion.getOwner();
            if (owner != null && !owner.isSpectator() && !owner.isCreative()) {
                event.setNewTarget(owner);
            } else {
                event.setNewTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!ModRecipes.CAULDRON_TYPE.isPresent())
            return;
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = event.getItemStack();
        if (state.is(Blocks.WATER_CAULDRON) && state.getValue(LayeredCauldronBlock.LEVEL) > 0) {
            Optional<CauldronRecipe> match = level.getRecipeManager()
                    .getAllRecipesFor(ModRecipes.CAULDRON_TYPE.get())
                    .stream()
                    .filter(recipe -> recipe.matches(stack))
                    .findFirst();
            if (match.isPresent()) {
                if (!level.isClientSide) {
                    Player player = event.getEntity();
                    CauldronRecipe recipe = match.get();
                    ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        player.setItemInHand(event.getHand(), result);
                    } else {
                        ItemEntity drop = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), result);
                        level.addFreshEntity(drop);
                    }
                    LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                    ((net.minecraft.server.level.ServerLevel) level).sendParticles(
                            net.minecraft.core.particles.ParticleTypes.CLOUD,
                            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            10, 0.2, 0.2, 0.2, 0.1);
                }
                event.setCanceled(true);
                event.getEntity().swing(event.getHand(), true);
            }
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getName().toString().equals("fdbosses:entities/malkuth")) {
            LootPool pool = LootPool.lootPool()
                    .add(LootItem.lootTableItem(ModItems.THE_SOVEREIGNTY.get()))
                    .when(LootItemRandomChanceCondition.randomChance(1))
                    .build();
            event.getTable().addPool(pool);
        }
        if (event.getName().toString().equals("fdbosses:entities/chesed")) {
            LootPool pool = LootPool.lootPool()
                    .add(LootItem.lootTableItem(ModItems.SERAPHIM_RAILGUN.get()))
                    .when(LootItemRandomChanceCondition.randomChance(1))
                    .build();
            event.getTable().addPool(pool);
        }
        if (event.getName().toString().equals("fdbosses:entities/fire_malkuth_warrior")) {
            LootPool pool = LootPool.lootPool()
                    .add(LootItem.lootTableItem(ModItems.KNIGHT_SCRAP.get()))
                    .when(LootItemRandomChanceCondition.randomChance(0.75f))
                    .build();
            event.getTable().addPool(pool);
        }
        if (event.getName().toString().equals("fdbosses:entities/ice_malkuth_warrior")) {
            LootPool pool = LootPool.lootPool()
                    .add(LootItem.lootTableItem(ModItems.KNIGHT_SCRAP.get()))
                    .when(LootItemRandomChanceCondition.randomChance(0.75f))
                    .build();
            event.getTable().addPool(pool);
        }
        if (event.getName().toString().equals("fdbosses:entities/geburah")) {
            LootPool pool = LootPool.lootPool()
                    .add(LootItem.lootTableItem(ModItems.GOLDEN_CORE.get()))
                    .when(LootItemRandomChanceCondition.randomChance(1))
                    .build();
            event.getTable().addPool(pool);
        }
    }
}