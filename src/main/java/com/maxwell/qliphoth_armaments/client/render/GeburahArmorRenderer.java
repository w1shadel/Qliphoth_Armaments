package com.maxwell.qliphoth_armaments.client.render;

import com.maxwell.qliphoth_armaments.client.model.GeburahModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class GeburahArmorRenderer {
    public static void register(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeburahModel<LivingEntity> model;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                    EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
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
                this.model.rightLeg.visible = equipmentSlot == EquipmentSlot.LEGS
                        || equipmentSlot == EquipmentSlot.FEET;
                this.model.leftLeg.visible = equipmentSlot == EquipmentSlot.LEGS || equipmentSlot == EquipmentSlot.FEET;
                float partialTick = Minecraft.getInstance().getPartialTick();
                this.model.tickCape(livingEntity, partialTick);
                return this.model;
            }
        });
    }
}
