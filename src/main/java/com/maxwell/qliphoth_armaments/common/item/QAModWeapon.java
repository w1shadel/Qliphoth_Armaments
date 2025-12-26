package com.maxwell.qliphoth_armaments.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface QAModWeapon {

    
    private void addSkillComponent(List<Component> tooltip, String titleKey, ChatFormatting titleFormatting, String... descriptionKeys) {
        tooltip.add(Component.translatable(titleKey).withStyle(titleFormatting));
        for (String key : descriptionKeys) {
            tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.empty());
    }

    
    default void addPassiveSkill(List<Component> tooltip, String titleKey, String descriptionKey) {
        addSkillComponent(tooltip, titleKey, ChatFormatting.GOLD, descriptionKey);
    }

    
    default void addRightClickSkill(List<Component> tooltip, String titleKey, String descriptionKey) {
        addSkillComponent(tooltip, titleKey, ChatFormatting.AQUA, descriptionKey);
    }

    
    default void addLongRightClickSkill(List<Component> tooltip, String titleKey, String... descriptionKeys) {
        addSkillComponent(tooltip, titleKey, ChatFormatting.RED, descriptionKeys);
    }

    
    default void addShiftRightClickSkill(List<Component> tooltip, String titleKey, String descriptionKey) {
        addSkillComponent(tooltip, titleKey, ChatFormatting.LIGHT_PURPLE, descriptionKey);
    }

    
    default void addCoreSkill(List<Component> tooltip, String descriptionKey) {
        tooltip.add(Component.translatable(descriptionKey));
    }

    
    default void addPressShiftHint(List<Component> tooltip) {
        tooltip.add(Component.translatable("item.qliphoth_armaments.press_shift").withStyle(ChatFormatting.DARK_GRAY));
    }

    
    default void addFuseHint(List<Component> tooltip, String coreItemNameKey) {
        tooltip.add(Component.translatable("item.qliphoth_armaments.fuse", Component.translatable(coreItemNameKey)));
    }
}