package com.maxwell.qliphoth_armaments.common.item;

import com.finderfeed.fdbosses.content.data_components.ItemCoreDataComponent;
import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSins;
import com.finderfeed.fdbosses.init.BossDataComponents;
import com.maxwell.qliphoth_armaments.client.model.GeburahModel;
import com.maxwell.qliphoth_armaments.common.util.GradientTextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class GeburahArmorItem extends ArmorItem implements QAModWeapon {

    public GeburahArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    public static final int MAX_REPAIR_TIME = 12000;
    private static final String REPAIR_TAG = "GeburahCoreRepairTimer";

    public static boolean hasJusticeCore(ItemStack stack) {
        ItemCoreDataComponent component = stack.get(BossDataComponents.ITEM_CORE);
        if (component != null) {
            return component.getCoreType() == ItemCoreDataComponent.CoreType.JUSTICE_CORE;
        }
        return false;
    }

    public static boolean isCoreBroken(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(REPAIR_TAG);
    }

    public static int getRepairTimer(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.contains(REPAIR_TAG) ? data.copyTag().getInt(REPAIR_TAG) : 0;
    }

    public static void setRepairTimer(ItemStack stack, int time) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(REPAIR_TAG, time));
    }

    public static void removeRepairTimer(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(REPAIR_TAG));
    }

    @Override
    public Component getName(ItemStack stack) {
        String translatedName = Component.translatable(this.getDescriptionId(stack)).getString();
        Color bloodRed = new Color(0x990000);
        Color brightRed = new Color(0xFF0000);
        Color darkRed = new Color(0x220000);
        return GradientTextUtil.createAnimatedGradient(
                translatedName,
                200,
                darkRed, bloodRed, brightRed, bloodRed, darkRed
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        boolean isAwakened = false;
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            PlayerSins sins = PlayerSins.getPlayerSins(player);
            if (sins != null && sins.getSinnedTimes() >= 7) {
                isAwakened = true;
            }
        }
        if (isAwakened) {
            tooltipComponents.add(Component.translatable("tooltip.qliphoth_armaments.geburah.lore2").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC, ChatFormatting.BOLD));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.qliphoth_armaments.geburah.lore1").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        }
        if (getType() == Type.CHESTPLATE) {
            boolean shiftDown = Screen.hasShiftDown();
            boolean ctrlDown = Screen.hasControlDown();
            if (hasJusticeCore(stack)) {
                tooltipComponents.add(Component.empty());
                tooltipComponents.add(Component.translatable("tooltip.qliphoth_armaments.geburah.passive.core.desc").withStyle(ChatFormatting.AQUA));
                tooltipComponents.add(Component.translatable("tooltip.qliphoth_armaments.geburah.passive.core.desc2").withStyle(ChatFormatting.GRAY));
                tooltipComponents.add(Component.translatable("tooltip.qliphoth_armaments.geburah.passive.core.desc3").withStyle(ChatFormatting.GRAY));
            }
            if (ctrlDown) {
                tooltipComponents.add(Component.empty());
                if (isAwakened) {
                    tooltipComponents.add(Component.translatable("tooltip.qliphoth_armaments.geburah.codex.title.awakened")
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD, ChatFormatting.UNDERLINE));
                    addSkillComponent(tooltipComponents,
                            "tooltip.qliphoth_armaments.geburah.sin.thirst", ChatFormatting.DARK_RED,
                            "tooltip.qliphoth_armaments.geburah.sin.thirst.desc");
                    addSkillComponent(tooltipComponents,
                            "tooltip.qliphoth_armaments.geburah.sin.restless", ChatFormatting.DARK_AQUA,
                            "tooltip.qliphoth_armaments.geburah.sin.restless.desc");
                    addSkillComponent(tooltipComponents,
                            "tooltip.qliphoth_armaments.geburah.sin.hubris", ChatFormatting.GOLD,
                            "tooltip.qliphoth_armaments.geburah.sin.hubris.desc");
                    addSkillComponent(tooltipComponents,
                            "tooltip.qliphoth_armaments.geburah.sin.voracity", ChatFormatting.DARK_PURPLE,
                            "tooltip.qliphoth_armaments.geburah.sin.voracity.desc");
                    tooltipComponents.add(Component.empty());
                    tooltipComponents.add(Component.translatable("tooltip.qliphoth_armaments.geburah.codex.hint.awakened")
                            .withStyle(ChatFormatting.RED, ChatFormatting.OBFUSCATED));

                } else {
                    tooltipComponents.add(Component.translatable("tooltip.qliphoth_armaments.geburah.codex.title")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE));
                    addSkillComponent(tooltipComponents, "tooltip.qliphoth_armaments.geburah.sin.wrath", ChatFormatting.RED, "tooltip.qliphoth_armaments.geburah.sin.wrath.desc");
                    addSkillComponent(tooltipComponents, "tooltip.qliphoth_armaments.geburah.sin.silence", ChatFormatting.AQUA, "tooltip.qliphoth_armaments.geburah.sin.silence.desc");
                    addSkillComponent(tooltipComponents, "tooltip.qliphoth_armaments.geburah.sin.pride", ChatFormatting.YELLOW, "tooltip.qliphoth_armaments.geburah.sin.pride.desc");
                    addSkillComponent(tooltipComponents, "tooltip.qliphoth_armaments.geburah.sin.envy", ChatFormatting.LIGHT_PURPLE, "tooltip.qliphoth_armaments.geburah.sin.envy.desc");
                    tooltipComponents.add(Component.empty());
                    tooltipComponents.add(Component.translatable("tooltip.qliphoth_armaments.geburah.codex.hint").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                }

            } else if (shiftDown) {
                addPassiveSkill(tooltipComponents, "tooltip.qliphoth_armaments.geburah.passive.arbiter", "tooltip.qliphoth_armaments.geburah.passive.arbiter.desc");
                addPassiveSkill(tooltipComponents, "tooltip.qliphoth_armaments.geburah.passive.red_mist", "tooltip.qliphoth_armaments.geburah.passive.red_mist.desc");
                tooltipComponents.add(Component.empty());
                tooltipComponents.add(Component.translatable("tooltip.qliphoth_armaments.geburah.warning").withStyle(ChatFormatting.RED));
                tooltipComponents.add(Component.empty());
                ChatFormatting hintColor = isAwakened ? ChatFormatting.RED : ChatFormatting.DARK_GRAY;
                tooltipComponents.add(Component.translatable("tooltip.qliphoth_armaments.press_ctrl").withStyle(hintColor));

            } else {
                addPressShiftHint(tooltipComponents);
            }
        }
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