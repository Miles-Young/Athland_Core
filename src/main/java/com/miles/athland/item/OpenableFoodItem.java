package com.miles.athland.item;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;

/**
 * 需要先「敲开」才能食用的食物。
 *
 * <p>未开壳形态刻意不带 {@code DataComponents.FOOD}：这样 {@link Item#use} 的
 * {@code foodproperties == null} 分支会先命中，不会进入进食动画，敲开逻辑得以独占右键。
 * 已开壳形态是普通 {@link Item}，走原版进食流程。
 *
 * <p>物品转换只在服务端执行，客户端只返回 {@code sidedSuccess}，避免客户端预测消耗导致物品复制。
 */
public class OpenableFoodItem extends Item {
    /** 敲开后的产物。用 Supplier 延迟求值，规避注册期 {@code DeferredHolder.get()} 的时序问题。 */
    private final Supplier<? extends Item> openedItem;

    public OpenableFoodItem(Supplier<? extends Item> openedItem, Properties properties) {
        super(properties);
        this.openedItem = openedItem;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        // 音效参数照抄原版投掷鸡蛋（EggItem.use）：客户端与服务端都播，音高带随机浮动。
        level.playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.EGG_THROW,
            SoundSource.PLAYERS,
            0.5F,
            0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        if (!level.isClientSide) {
            // 复用原版桶装液体的转换范式：消耗 1 个未开壳的，产出 1 个剥壳的。
            // 它处理了「手上只剩最后一个 → 剥壳蛋直接顶到手上」与「背包满 → 掉落地上」，
            // 且 consume 在创造模式下不扣物品。
            //
            // preventDuplicates 必须传 false：三参重载默认 true，那是桶装液体的语义 ——
            // 创造模式下「背包里已有同款物品就不再添加」，会导致敲开蛋永远产不出新的剥壳蛋。
            ItemStack opened = ItemUtils.createFilledResult(stack, player, new ItemStack(openedItem.get()), false);
            return InteractionResultHolder.sidedSuccess(opened, level.isClientSide());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
