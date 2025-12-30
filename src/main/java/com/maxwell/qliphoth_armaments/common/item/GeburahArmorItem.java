package com.maxwell.qliphoth_armaments.common.item;

import com.maxwell.qliphoth_armaments.client.model.GeburahModel;
import com.maxwell.qliphoth_armaments.init.ModAttachments;
import com.maxwell.qliphoth_armaments.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class GeburahArmorItem extends ArmorItem {

    public GeburahArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            boolean hasSet = hasFullSuitOfArmorOn(player);
            int sin = player.getData(ModAttachments.SIN.get());
            if (player.getItemBySlot(EquipmentSlot.FEET).is(this) && sin <= 2) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1, 0, false, false));
            }
            if (player.getItemBySlot(EquipmentSlot.FEET).is(this) && sin >= 10) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1, 0, false, false));
            }
            if (hasSet) {
                if (sin >= 7) {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1, 1, false, false));
                    if (player.tickCount % 40 == 0) {
                    }
                }
                if (sin >= 12) {
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 4));
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                    player.setData(ModAttachments.SIN.get(), 0);
                    level.explode(player, player.getX(), player.getY(), player.getZ(), 2.0f, Level.ExplosionInteraction.NONE);
                    player.displayClientMessage(Component.literal("§4§l断罪！！"), true);
                }
            }
        }
    }

    private boolean hasFullSuitOfArmorOn(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GEBURAH_HELMET.get()) &&
                player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.GEBURAH_CHESTPLATE.get()) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.GEBURAH_LEGGINGS.get()) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.GEBURAH_BOOTS.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (getType() == Type.CHESTPLATE) {
            tooltipComponents.add(Component.literal("Shift+右クリックで罪状を変更").withStyle(ChatFormatting.GRAY));
        }
        tooltipComponents.add(Component.literal("罪と共に在れ").withStyle(ChatFormatting.DARK_RED));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeburahModel<LivingEntity> model;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.model == null) {
                    var layer = Minecraft.getInstance().getEntityModels().bakeLayer(GeburahModel.LAYER_LOCATION);
                    this.model = new GeburahModel<>(layer);
                }
                this.model.setupAnim(livingEntity, 0f, 0f, 0f, 0f, 0f);
                this.model.prepareMobModel(livingEntity, 0f, 0f, 0f);
                this.model.crouching = original.crouching;
                this.model.riding = original.riding;
                this.model.young = original.young;
                this.model.head.visible = equipmentSlot == EquipmentSlot.HEAD;
                this.model.body.visible = equipmentSlot == EquipmentSlot.CHEST;
                this.model.rightArm.visible = equipmentSlot == EquipmentSlot.CHEST;
                this.model.leftArm.visible = equipmentSlot == EquipmentSlot.CHEST;
                this.model.rightLeg.visible = equipmentSlot == EquipmentSlot.LEGS || equipmentSlot == EquipmentSlot.FEET;
                this.model.leftLeg.visible = equipmentSlot == EquipmentSlot.LEGS || equipmentSlot == EquipmentSlot.FEET;
                float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                this.model.tickCape(livingEntity, partialTick);
                return this.model;
            }
        });
    }
}
