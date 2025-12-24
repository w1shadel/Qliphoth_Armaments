package com.maxwell.qliphoth_armaments.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface QAModWeapon {

    /**
     * スキル説明の定型文をTooltipに追加する汎用メソッド。
     *
     * @param tooltip         ツールチップリスト
     * @param titleKey        スキル名の翻訳キー (例: "item.qliphoth_armaments.conductors_requiem.r_skill_1")
     * @param titleFormatting スキル名の色やスタイル
     * @param descriptionKeys スキル説明の翻訳キー (複数行対応)
     */
    private void addSkillComponent(List<Component> tooltip, String titleKey, ChatFormatting titleFormatting, String... descriptionKeys) {
        tooltip.add(Component.translatable(titleKey).withStyle(titleFormatting));
        for (String key : descriptionKeys) {
            tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.empty());
    }

    /**
     * パッシブスキルの説明を追加します。 (色: GOLD)
     *
     * @param tooltip        ツールチップリスト
     * @param titleKey       パッシブスキル名の翻訳キー
     * @param descriptionKey パッシブスキルの説明の翻訳キー
     */
    default void addPassiveSkill(List<Component> tooltip, String titleKey, String descriptionKey) {
        addSkillComponent(tooltip, titleKey, ChatFormatting.GOLD, descriptionKey);
    }

    /**
     * 右クリックアクションのスキル説明を追加します。 (色: AQUA)
     *
     * @param tooltip        ツールチップリスト
     * @param titleKey       スキル名の翻訳キー
     * @param descriptionKey スキル説明の翻訳キー
     */
    default void addRightClickSkill(List<Component> tooltip, String titleKey, String descriptionKey) {
        addSkillComponent(tooltip, titleKey, ChatFormatting.AQUA, descriptionKey);
    }

    /**
     * 右クリック長押しアクションのスキル説明を追加します。 (色: RED)
     *
     * @param tooltip         ツールチップリスト
     * @param titleKey        スキル名の翻訳キー
     * @param descriptionKeys スキル説明の翻訳キー（複数行対応）
     */
    default void addLongRightClickSkill(List<Component> tooltip, String titleKey, String... descriptionKeys) {
        addSkillComponent(tooltip, titleKey, ChatFormatting.RED, descriptionKeys);
    }

    /**
     * しゃがみ + 右クリックアクションのスキル説明を追加します。 (色: LIGHT_PURPLE)
     *
     * @param tooltip        ツールチップリスト
     * @param titleKey       スキル名の翻訳キー
     * @param descriptionKey スキル説明の翻訳キー
     */
    default void addShiftRightClickSkill(List<Component> tooltip, String titleKey, String descriptionKey) {
        addSkillComponent(tooltip, titleKey, ChatFormatting.LIGHT_PURPLE, descriptionKey);
    }

    /**
     * コアによる強化スキル（fuse skill）の説明を追加します。
     *
     * @param tooltip        ツールチップリスト
     * @param descriptionKey 強化内容の説明の翻訳キー
     */
    default void addCoreSkill(List<Component> tooltip, String descriptionKey) {
        tooltip.add(Component.translatable(descriptionKey));
    }

    /**
     * 「Shiftキーで詳細表示」の定型文を追加します。
     *
     * @param tooltip ツールチップリスト
     */
    default void addPressShiftHint(List<Component> tooltip) {
        tooltip.add(Component.translatable("item.qliphoth_armaments.press_shift").withStyle(ChatFormatting.DARK_GRAY));
    }

    /**
     * コアとの合成を促す定型文を追加します。
     *
     * @param tooltip         ツールチップリスト
     * @param coreItemNameKey コアアイテム名の翻訳キー
     */
    default void addFuseHint(List<Component> tooltip, String coreItemNameKey) {
        tooltip.add(Component.translatable("item.qliphoth_armaments.fuse", Component.translatable(coreItemNameKey)));
    }
}