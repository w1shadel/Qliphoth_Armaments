package com.maxwell.qliphoth_armaments.common.event;

import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSins;
import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.item.GeburahArmorItem;
import com.maxwell.qliphoth_armaments.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = QA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArmorEventHandler {
    @SubscribeEvent
    public static void onDamagePre(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && hasFullArmor(player)) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (GeburahArmorItem.hasJusticeCore(chest)) {
                // コアがある間は被ダメージを30%カット
                event.setAmount(event.getAmount() * 0.7F);
            }
        }
    }

    // ★重要: あらゆる「死」を無効化し、罪を0にして復活させる
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && hasFullArmor(player)) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            // コアを持っている場合のみ発動
            if (GeburahArmorItem.hasJusticeCore(chest)) {
                // 1. 死亡をキャンセル（罪による即死ダメージもこれで止まる）
                event.setCanceled(true);
                // 2. 復活処理の実行
                PlayerSins sins = PlayerSins.getPlayerSins(player);
                consumeCoreAndReset(player, chest, sins);
            }
        }
    }

    public static void consumeCoreAndReset(ServerPlayer player, ItemStack chest, PlayerSins sins) {
        // コア（item_coreタグ）を削除して「壊れた状態」にする
        GeburahArmorItem.removeCore(chest);
        // 再充填（修理）タイマーをセット
        GeburahArmorItem.setRepairTimer(chest, GeburahArmorItem.MAX_REPAIR_TIME);
        // 罪（Sin）を完全に0にリセットする
        if (sins != null) {
            sins.setSinnedTimes(0);
            PlayerSins.setPlayerSins(player, sins);
        }
        // 体力を最大まで全回復
        player.setHealth(player.getMaxHealth());
        // 通知メッセージ
        player.displayClientMessage(Component.translatable("tooltip.qliphoth_armaments.geburah.passive.broken_core.desc").withStyle(ChatFormatting.AQUA), true);
        player.displayClientMessage(Component.translatable("tooltip.qliphoth_armaments.geburah.passive.time_heal_core").withStyle(ChatFormatting.GRAY), false);
        // 復活エフェクトと音
        player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.level().playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5f, 0.5f);
        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1, player.getZ(), 3, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1, player.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
        }
        // 周囲への衝撃波
        AABB burstArea = player.getBoundingBox().inflate(8.0);
        var enemies = player.level().getEntitiesOfClass(LivingEntity.class, burstArea, e -> e != player && e.isAlive());
        for (LivingEntity e : enemies) {
            e.hurt(player.damageSources().magic(), 20.0f);
            double dx = e.getX() - player.getX();
            double dz = e.getZ() - player.getZ();
            e.knockback(2.5, -dx, -dz);
        }
    }

    private static boolean hasFullArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GEBURAH_HELMET.get()) &&
                player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.GEBURAH_CHESTPLATE.get()) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.GEBURAH_LEGGINGS.get()) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.GEBURAH_BOOTS.get());
    }
}