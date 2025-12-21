package com.Maxwell.qliphoth_armaments.common.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class ChesedOrchestratorItem extends SwordItem {

    public ChesedOrchestratorItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    // 1. パッシブ効果：持っている間（インベントリにある間）の処理
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && isSelected) { // 手に持っている時
            // ここに「ミニオン（コア1体）」を召喚・維持する処理
            // 例: プレイヤーにタグをつけて、イベント側でミニオンを管理するなど
        }
    }

    // 2. アクティブ効果：右クリック（指揮）した時の処理
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            // 音を鳴らす（本体Modの音を参照！）
            level.playSound(null, player.blockPosition(),
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("fdbosses", "chesed_lightning_ray"))),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            // 必殺技：4体展開＆レーザー発射のロジック
            // ここで強力なEntityを発射するか、RayTraceで範囲攻撃を行う
            performGrandSymphonyAttack(player, level);
            // クールタイム設定
            player.getCooldowns().addCooldown(this, 200); // 10秒クールダウン
        }
        return InteractionResultHolder.success(stack);
    }

    // 攻撃処理の中身
    private void performGrandSymphonyAttack(Player player, Level level) {
        // ここに「ドカーン！」となる処理を書く
        // パーティクルも本体のものを参照して出すと綺麗です
    }
}