package com.maxwell.qliphoth_armaments.common.event;

import com.finderfeed.fdbosses.init.BossSounds;
import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.init.ModAttachments;
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
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = QA.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SinManager {

    public static final int MODE_WRATH = 0;
    public static final int MODE_SILENCE = 1;
    public static final int MODE_PRIDE = 2;
    public static final int MODE_ENVY = 3;

    private static final Random random = new Random();
    private static final int SWITCH_INTERVAL = 300;
    private static final double EFFECT_RADIUS = 16.0;

    // ★追加: 罪を犯した後の無敵時間 (tick)
    // 本家では40tick (2秒) 程度設定されることが多いです
    private static final int DEFAULT_SIN_COOLDOWN = 40;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // ★追加: クールダウンの減算処理 (PlayerSinsHandler.tickPlayerSins を参考)
            int currentCooldown = player.getData(ModAttachments.SIN_COOLDOWN);
            if (currentCooldown > 0) {
                player.setData(ModAttachments.SIN_COOLDOWN, currentCooldown - 1);
            }
            if (!hasFullArmor(player)) return;
            // ... 既存のモード切替ロジック ...
            int timer = player.getData(ModAttachments.SIN_TIMER);
            timer++;
            if (timer >= SWITCH_INTERVAL) {
                switchMode(player);
                timer = 0;
            }
            player.setData(ModAttachments.SIN_TIMER, timer);
            if (player.tickCount % 5 == 0) {
                int mode = player.getData(ModAttachments.SIN_MODE);
                checkAndApplySin(player, player, mode);
                AABB area = player.getBoundingBox().inflate(EFFECT_RADIUS);
                List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                        e -> e != player && e.isAlive());
                for (LivingEntity target : targets) {
                    checkAndApplySin(player, target, mode);
                }
            }
        }
    }

    // ... onDamage, onItemUseStart はそのまま ...
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ? (LivingEntity) event.getSource().getEntity() : null;
        if (attacker != null && !attacker.level().isClientSide) {
            ServerPlayer arbiter = findNearbyArbiter(attacker);
            if (arbiter != null) {
                int mode = arbiter.getData(ModAttachments.SIN_MODE);
                if (mode == MODE_WRATH) {
                    addSinToEntity(arbiter, attacker, 1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        LivingEntity user = event.getEntity();
        if (!user.level().isClientSide) {
            ServerPlayer arbiter = findNearbyArbiter(user);
            if (arbiter != null) {
                int mode = arbiter.getData(ModAttachments.SIN_MODE);
                if (mode == MODE_ENVY) {
                    addSinToEntity(arbiter, user, 2);
                }
            }
        }
    }

    private static void checkAndApplySin(ServerPlayer arbiter, LivingEntity target, int mode) {
        // ... 判定ロジックはそのまま ...
        boolean sinned = false;
        if (mode == MODE_SILENCE) {
            double motion = target.getDeltaMovement().horizontalDistanceSqr();
            if (motion > 0.002) {
                sinned = true;
                if (target instanceof Player p && p.tickCount % 20 == 0) { // メッセージスパム防止
                    p.displayClientMessage(Component.literal("§b[静寂] 動いてはならない...").withStyle(ChatFormatting.AQUA), true);
                }
            }
        } else if (mode == MODE_PRIDE) {
            if (!target.onGround() && !target.isInWater() && !target.isFallFlying()) {
                sinned = true;
                if (target instanceof Player p && p.tickCount % 20 == 0) {
                    p.displayClientMessage(Component.literal("§e[傲慢] 地に伏せよ...").withStyle(ChatFormatting.YELLOW), true);
                }
            }
        }
        if (sinned) {
            addSinToEntity(arbiter, target, 1);
        }
    }

    private static void addSinToEntity(ServerPlayer arbiter, LivingEntity target, int amount) {
        // ★追加: クールダウンチェック (PlayerSinsHandler.sin を参考)
        // ターゲットがプレイヤーの場合、アタッチメントのクールダウンを確認
        if (target instanceof Player) {
            int cooldown = target.getData(ModAttachments.SIN_COOLDOWN);
            if (cooldown > 0) {
                return; // クールダウン中なら何もしない
            }
        }
        int maxSin = 6;
        if (target instanceof Player player && hasFullArmor(player)) {
            maxSin = 12;
        }
        int currentSin = target.getData(ModAttachments.SIN);
        int newSin = currentSin + amount; // ここではキャップせず、判定後に処理
        // 罪が増える処理
        target.setData(ModAttachments.SIN, Math.min(newSin, maxSin));
        if (target instanceof Player) {
            target.setData(ModAttachments.SIN_COOLDOWN, DEFAULT_SIN_COOLDOWN);
        }
        if (target.level() instanceof ServerLevel serverLevel) {
            try {
                if (BossSounds.GEBURAH_SIN != null) {
                    serverLevel.playSound(null, target.blockPosition(), BossSounds.GEBURAH_SIN.get(), SoundSource.HOSTILE, 0.5f, 1.5f);
                }
            } catch (NoClassDefFoundError | Exception e) {
                serverLevel.playSound(null, target.blockPosition(), SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.HOSTILE, 0.5f, 2.0f);
            }
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    5, 0.2, 0.2, 0.2, 0.05);
        }
        // 即死判定 (PlayerSinsHandler.sin 内の hurt(..., Float.MAX_VALUE) を参考)
        if (newSin >= maxSin) {
            executeJudgment(arbiter, target);
        }
    }

    private static void executeJudgment(ServerPlayer arbiter, LivingEntity target) {
        target.setData(ModAttachments.SIN, 0);
        // 即死後はクールダウンもリセットしても良い
        if (target instanceof Player) {
            target.setData(ModAttachments.SIN_COOLDOWN, 0);
        }
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.0f, 1.0f);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    target.getX(), target.getY(), target.getZ(),
                    1, 0, 0, 0, 0);
        }
        // ★変更: 本家に倣って確定即死ダメージに変更
        target.hurt(target.damageSources().magic(), Float.MAX_VALUE);
        if (target instanceof Player p) {
            p.sendSystemMessage(Component.literal("§4§l≪ 断罪 ≫ 汝の罪は満ちた。").withStyle(ChatFormatting.DARK_RED));
        } else {
            arbiter.displayClientMessage(Component.literal("§4[断罪] " + target.getName().getString() + " を処罰しました。"), true);
        }
    }

    // ... findNearbyArbiter, switchMode, hasFullArmor はそのまま ...
    private static ServerPlayer findNearbyArbiter(LivingEntity entity) {
        AABB searchArea = entity.getBoundingBox().inflate(EFFECT_RADIUS);
        List<ServerPlayer> nearbyPlayers = entity.level().getEntitiesOfClass(ServerPlayer.class, searchArea);
        for (ServerPlayer p : nearbyPlayers) {
            if (hasFullArmor(p)) {
                return p;
            }
        }
        return null;
    }

    private static void switchMode(ServerPlayer player) {
        int nextMode = random.nextInt(4);
        player.setData(ModAttachments.SIN_MODE, nextMode);
        String modeName = switch (nextMode) {
            case MODE_WRATH -> "§c§l憤怒 (攻撃禁止)";
            case MODE_SILENCE -> "§b§l静寂 (移動禁止)";
            case MODE_PRIDE -> "§e§l傲慢 (滞空禁止)";
            case MODE_ENVY -> "§d§l嫉妬 (使用禁止)";
            default -> "審判";
        };
        player.sendSystemMessage(Component.literal("§7§l[法典] 新たな罪状が制定されました： " + modeName));
        player.level().playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 2.0f, 0.5f);
    }

    private static boolean hasFullArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GEBURAH_HELMET.get()) &&
                player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.GEBURAH_CHESTPLATE.get()) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.GEBURAH_LEGGINGS.get()) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.GEBURAH_BOOTS.get());
    }
}