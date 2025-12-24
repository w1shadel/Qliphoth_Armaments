package com.maxwell.qliphoth_armaments.common;

import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.entity.ChesedCoreMinionEntity;
import com.maxwell.qliphoth_armaments.common.item.ConductorRequiemItem;
import com.maxwell.qliphoth_armaments.common.recipe.CauldronRecipe;
import com.maxwell.qliphoth_armaments.init.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Optional;

@EventBusSubscriber(modid = QA.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CommonEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        ConductorRequiemItem.onPlayerTick(event.getEntity());
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getOriginalAboutToBeSetTarget();
        if (newTarget instanceof ChesedCoreMinionEntity minion) {
            Player owner = minion.getOwner();
            if (owner != null && !owner.isSpectator() && !owner.isCreative()) {
                event.setNewAboutToBeSetTarget(owner);
            } else {
                event.setNewAboutToBeSetTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = event.getItemStack();
        if (state.is(Blocks.WATER_CAULDRON) && state.getValue(LayeredCauldronBlock.LEVEL) > 0) {
            SingleRecipeInput input = new SingleRecipeInput(stack);
            Optional<RecipeHolder<CauldronRecipe>> match = level.getRecipeManager()
                    .getRecipeFor(ModRecipes.CAULDRON_TYPE.get(), input, level);
            if (match.isPresent()) {
                if (!level.isClientSide) {
                    Player player = event.getEntity();
                    RecipeHolder<CauldronRecipe> holder = match.get();
                    CauldronRecipe recipe = holder.value();
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
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CLOUD,
                                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                                10, 0.2, 0.2, 0.2, 0.1);
                    }
                }
                event.setCanceled(true);
                event.getEntity().swing(event.getHand(), true);
            }
        }
    }
}