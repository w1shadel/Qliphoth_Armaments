package com.maxwell.qliphoth_armaments.common.event;

import com.finderfeed.fdbosses.content.entities.geburah.GeburahEntity;
import com.finderfeed.fdbosses.content.entities.geburah.sins.PlayerSinsHandler;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.ActivePlayerSinInstance;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSin;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSins;
import com.finderfeed.fdbosses.content.util.WorldBox;
import com.finderfeed.fdbosses.init.BossDamageSources;
import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.api.SinVisualManager;
import com.maxwell.qliphoth_armaments.api.capabilities.CapabilityHandler;
import com.maxwell.qliphoth_armaments.api.capabilities.EntityMiscData;
import com.maxwell.qliphoth_armaments.common.item.GeburahArmorItem;
import com.maxwell.qliphoth_armaments.common.network.PacketHandler;
import com.maxwell.qliphoth_armaments.common.network.PacketSyncMobSin;
import com.maxwell.qliphoth_armaments.init.ModItems;
import com.maxwell.qliphoth_armaments.init.ModSins;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = QA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SinManager {
    private static final Random random = new Random();
    private static final int SWITCH_INTERVAL = 300;
    private static final double EFFECT_RADIUS = 24.0;
    private static final int COMBAT_TIMEOUT = 200;
    private static final int REDUCTION_INTERVAL = 2400;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (isFightingGeburah(player)) return;
        PlayerSins sinsCapability = PlayerSins.getPlayerSins(player);
        int sinnedTimes = (sinsCapability != null) ? sinsCapability.getSinnedTimes() : 0;
        boolean isArbiter = hasFullArmor(player);
        EntityMiscData miscData = player.getCapability(CapabilityHandler.MISC_DATA_CAPABILITY).orElse(null);
        if (miscData == null) return;
        if (sinnedTimes == 0 && isArbiter) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (GeburahArmorItem.isCoreBroken(chest)) {
                int repairTimer = GeburahArmorItem.getRepairTimer(chest);
                repairTimer--;
                if (repairTimer <= 0) {
                    GeburahArmorItem.removeRepairTimer(chest);
                    GeburahArmorItem.setJusticeCore(chest);
                    player.displayClientMessage(Component.translatable("tooltip.qliphoth_armaments.geburah.passive.recoverd").withStyle(ChatFormatting.AQUA), true);
                    player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                    }
                } else {
                    GeburahArmorItem.setRepairTimer(chest, repairTimer);
                }
            }
        }
        if (sinnedTimes > 0) {
            long lastCombat = miscData.getLastCombatTime();
            long currentTime = player.level().getGameTime();
            if (currentTime - lastCombat > COMBAT_TIMEOUT) {
                int reduceTimer = miscData.getSinReductionTimer();
                reduceTimer++;
                if (reduceTimer >= REDUCTION_INTERVAL) {
                    reduceSin(player, sinsCapability);
                    reduceTimer = 0;
                }
                miscData.setSinReductionTimer(reduceTimer);
            } else {
                miscData.setSinReductionTimer(0);
            }
        } else {
            miscData.setSinReductionTimer(0);
        }
        if (sinnedTimes >= 6) {
            if (sinnedTimes >= 12) {
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                if (isArbiter && GeburahArmorItem.hasJusticeCore(chest)) {
                    ArmorEventHandler.consumeCoreAndReset(player, chest, sinsCapability);
                    return;
                }
                player.hurt(BossDamageSources.GEBURAH_SINNED_TOO_MUCH_SOURCE, Float.MAX_VALUE);
                return;
            }
            if (!isArbiter) {
                sinsCapability.setSinnedTimes(0);
                PlayerSins.setPlayerSins(player, sinsCapability);
                SinVisualManager.sendJudgmentEffect(player);
                player.hurt(BossDamageSources.GEBURAH_SINNED_TOO_MUCH_SOURCE, Float.MAX_VALUE);
                return;
            }
            SinVisualManager.spawnAwakeningAura(player);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 1, false, false));
            AABB glowArea = player.getBoundingBox().inflate(16.0);
            player.level().getEntitiesOfClass(LivingEntity.class, glowArea, e -> e != player && e.isAlive())
                    .forEach(enemy -> enemy.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false)));
        } else {
            if (!isArbiter) return;
        }
        int timer = miscData.getSinTimer();
        timer++;
        if (timer >= SWITCH_INTERVAL) {
            switchMode(player, sinnedTimes);
            timer = 0;
        }
        miscData.setSinTimer(timer);
        if (player.tickCount % 20 == 0) {
            syncSinsToNearbyEntities(player);
            applyContinuousSinsToMobs(player);
        }
    }

    private static void reduceSin(ServerPlayer player, PlayerSins sins) {
        int current = sins.getSinnedTimes();
        if (current > 0) {
            sins.setSinnedTimes(current - 1);
            PlayerSins.setPlayerSins(player, sins);
            player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.0F);
        }
    }

    private static void syncSinsToNearbyEntities(ServerPlayer arbiter) {
        AABB area = arbiter.getBoundingBox().inflate(EFFECT_RADIUS);
        List<ServerPlayer> players = arbiter.level().getEntitiesOfClass(ServerPlayer.class, area, e -> e != arbiter && e.isAlive());
        PlayerSins arbiterSins = PlayerSins.getPlayerSins(arbiter);
        if (arbiterSins == null) return;
        List<ActivePlayerSinInstance> activeInstances = arbiterSins.getActiveSins();
        if (activeInstances.isEmpty()) return;
        for (ServerPlayer target : players) {
            PlayerSins targetSins = PlayerSins.getPlayerSins(target);
            if (!isSameSins(arbiterSins, targetSins)) {
                targetSins.setActiveSins(new ArrayList<>(activeInstances));
                PlayerSins.setPlayerSins(target, targetSins);
            }
        }
    }

    private static void applyContinuousSinsToMobs(ServerPlayer arbiter) {
        AABB area = arbiter.getBoundingBox().inflate(EFFECT_RADIUS);
        List<LivingEntity> entities = arbiter.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != arbiter && e.isAlive());
        PlayerSins arbiterSins = PlayerSins.getPlayerSins(arbiter);
        if (arbiterSins == null) return;
        if (arbiterSins.hasSinActive(ModSins.PRIDE.get())) {
            for (LivingEntity entity : entities) {
                if (entity instanceof ServerPlayer) continue;
                if (entity.hurtTime > 0) continue;
                if (!entity.onGround() && !entity.isInWater()) {
                    applySinToEntity(entity, 1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        LivingEntity attackerEntity = event.getSource().getEntity() instanceof LivingEntity ? (LivingEntity) event.getSource().getEntity() : null;
        if (attackerEntity instanceof ServerPlayer attacker) {
            if (isFightingGeburah(attacker)) return;
            attacker.getCapability(CapabilityHandler.MISC_DATA_CAPABILITY).ifPresent(data -> {
                data.setLastDamageTime(attacker.level().getGameTime());
            });
            PlayerSins attackerSins = PlayerSins.getPlayerSins(attacker);
            int sinnedTimes = (attackerSins != null) ? attackerSins.getSinnedTimes() : 0;
            if (sinnedTimes >= 7 && hasFullArmor(attacker)) {
                float damage = event.getAmount();
                if (damage > 0) attacker.heal(damage * 0.1F);
            }
            if (attackerSins != null) {
                if (attackerSins.hasSinActive(ModSins.THIRST.get())) {
                    attacker.heal(event.getAmount() * 0.2F);
                }
                if (attackerSins.hasSinActive(ModSins.VORACITY.get()) && victim.isDeadOrDying()) {
                    attacker.getFoodData().eat(4, 2.0F);
                    attacker.level().playSound(null, attacker.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                if (attackerSins.hasSinActive(ModSins.WRATH.get())) {
                    PlayerSinsHandler.sin(attacker, 20);
                }
            }
        } else if (attackerEntity != null && !attackerEntity.level().isClientSide) {
            ServerPlayer arbiter = findNearbyArbiter(attackerEntity);
            if (arbiter != null) {
                PlayerSins arbiterSins = PlayerSins.getPlayerSins(arbiter);
                if (arbiterSins != null) {
                    if (arbiterSins.hasSinActive(ModSins.WRATH.get())) {
                        applySinToEntity(attackerEntity, 1);
                    }
                    if (arbiterSins.hasSinActive(ModSins.THIRST.get())) {
                        attackerEntity.heal(event.getAmount() * 0.1F);
                    }
                    if (arbiterSins.hasSinActive(ModSins.ENVY.get())) {
                        AABB checkArea = attackerEntity.getBoundingBox().inflate(5.0);
                        List<Mob> allies = attackerEntity.level().getEntitiesOfClass(Mob.class, checkArea,
                                e -> e != attackerEntity && e.isAlive() && e instanceof net.minecraft.world.entity.monster.Enemy);
                        if (!allies.isEmpty()) {
                            applySinToEntity(attackerEntity, 1);
                        }
                    }
                }
            }
        }
        if (!victim.level().isClientSide) {
            ServerPlayer arbiter = (victim instanceof ServerPlayer sp) ? sp : findNearbyArbiter(victim);
            if (arbiter != null && hasFullArmor(arbiter)) {
                PlayerSins arbiterSins = PlayerSins.getPlayerSins(arbiter);
                if (arbiterSins != null && arbiterSins.hasSinActive(ModSins.HUBRIS.get())) {
                    checkBackAttack(victim, event.getSource().getSourcePosition());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof ServerPlayer user) {
            if (isFightingGeburah(user)) return;
            user.getCapability(CapabilityHandler.MISC_DATA_CAPABILITY).ifPresent(data -> {
                data.setLastEatTime(user.level().getGameTime());
            });
            PlayerSins playerSins = PlayerSins.getPlayerSins(user);
            if (playerSins != null && playerSins.hasSinActive(ModSins.ENVY.get())) {
                PlayerSinsHandler.sin(user, 20);
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (event.getEntity() instanceof Projectile projectile) {
            if (projectile.getOwner() instanceof LivingEntity shooter && !(shooter instanceof Player)) {
                ServerPlayer arbiter = findNearbyArbiter(shooter);
                if (arbiter != null) {
                    PlayerSins sins = PlayerSins.getPlayerSins(arbiter);
                    if (sins != null && sins.hasSinActive(ModSins.SILENCE.get())) {
                        applySinToEntity(shooter, 1);
                    }
                }
            }
        }
    }

    public static void applySinToEntity(LivingEntity entity, int amount) {
        if (entity instanceof ServerPlayer sp) {
            PlayerSinsHandler.sin(sp, 20);
        } else {
            entity.getCapability(CapabilityHandler.MISC_DATA_CAPABILITY).ifPresent(data -> {
                int current = data.getSin();
                int next = Math.min(current + amount, 6);
                data.setSin(next);
                if (!entity.level().isClientSide) {
                    PacketSyncMobSin packet = new PacketSyncMobSin(entity.getId(), next);
                    PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
                }
                if (next >= 6) {
                    executeMobJudgment(entity);
                }
            });
        }
    }

    private static void executeMobJudgment(LivingEntity entity) {
        entity.getCapability(CapabilityHandler.MISC_DATA_CAPABILITY).ifPresent(data -> {
            data.setSin(0);
        });
        if (!entity.level().isClientSide) {
            PacketSyncMobSin packet = new PacketSyncMobSin(entity.getId(), 0);
            PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
        }
        SinVisualManager.sendJudgmentEffect(entity);
        float damage = entity.getMaxHealth() * 0.25F;
        entity.hurt(entity.damageSources().magic(), damage);
    }

    private static boolean isSameSins(PlayerSins s1, PlayerSins s2) {
        if (s1.getActiveSins().size() != s2.getActiveSins().size()) return false;
        if (s1.getActiveSins().isEmpty()) return true;
        return s1.getActiveSins().get(0).getSin() == s2.getActiveSins().get(0).getSin();
    }

    private static void checkBackAttack(LivingEntity victim, Vec3 sourcePos) {
        if (sourcePos == null) return;
        Vec3 look = victim.getLookAngle();
        Vec3 dist = sourcePos.subtract(victim.position()).normalize();
        if (look.dot(dist) < -0.3) {
            applySinToEntity(victim, 1);
        }
    }

    public static void switchMode(ServerPlayer player, int currentSins) {
        List<PlayerSin> nextSins = new ArrayList<>();
        if (currentSins < 7) {
            nextSins.add(getRandomSin(false));
        } else {
            nextSins.add(getRandomSin(true));
            if (currentSins >= 10) {
                PlayerSin s2 = getRandomSin(true);
                int retry = 0;
                while (s2 == nextSins.get(0) && retry < 10) {
                    s2 = getRandomSin(true);
                    retry++;
                }
                nextSins.add(s2);
            }
        }
        applySinsToPlayer(player, nextSins);
        float pitch = (currentSins >= 7) ? 0.5f : 1.2f;
        player.level().playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.5f, pitch);
        player.getCapability(CapabilityHandler.MISC_DATA_CAPABILITY).ifPresent(data -> {
            data.setSinTimer(0);
        });
        System.out.println("[QA-DEBUG] Sin Mode Switched for " + player.getName().getString());
    }

    private static PlayerSin getRandomSin(boolean isUpper) {
        int r = random.nextInt(4);
        if (isUpper) {
            return switch (r) {
                case 0 -> ModSins.THIRST.get();
                case 1 -> ModSins.RESTLESS.get();
                case 2 -> ModSins.HUBRIS.get();
                case 3 -> ModSins.VORACITY.get();
                default -> ModSins.THIRST.get();
            };
        } else {
            return switch (r) {
                case 0 -> ModSins.WRATH.get();
                case 1 -> ModSins.SILENCE.get();
                case 2 -> ModSins.PRIDE.get();
                case 3 -> ModSins.ENVY.get();
                default -> ModSins.WRATH.get();
            };
        }
    }

    private static void applySinsToPlayer(ServerPlayer player, List<PlayerSin> sins) {
        PlayerSins playerSins = PlayerSins.getPlayerSins(player);
        WorldBox box = new WorldBox(player.level().dimension(), new AABB(-3E7, -3E7, -3E7, 3E7, 3E7, 3E7));
        List<ActivePlayerSinInstance> list = new ArrayList<>();
        for (PlayerSin sin : sins) {
            ActivePlayerSinInstance inst = new ActivePlayerSinInstance(sin, box, 0);
            list.add(inst);
            sin.onSinAdded(player, inst);
        }
        playerSins.setActiveSins(list);
        PlayerSins.setPlayerSins(player, playerSins);
    }

    private static boolean isFightingGeburah(ServerPlayer player) {
        AABB checkArea = player.getBoundingBox().inflate(100.0);
        return !player.level().getEntitiesOfClass(GeburahEntity.class, checkArea, LivingEntity::isAlive).isEmpty();
    }

    private static boolean hasFullArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GEBURAH_HELMET.get()) &&
                player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.GEBURAH_CHESTPLATE.get()) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.GEBURAH_LEGGINGS.get()) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.GEBURAH_BOOTS.get());
    }

    private static ServerPlayer findNearbyArbiter(LivingEntity entity) {
        AABB searchArea = entity.getBoundingBox().inflate(EFFECT_RADIUS);
        List<ServerPlayer> players = entity.level().getEntitiesOfClass(ServerPlayer.class, searchArea);
        for (ServerPlayer p : players) {
            if (hasFullArmor(p)) return p;
        }
        return null;
    }
}