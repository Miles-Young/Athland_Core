package com.miles.athland.event;

import com.miles.athland.Athland;
import com.miles.athland.component.AthlandComponents;
import com.miles.athland.component.OxygenRestore;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

/**
 * 物品使用完毕后的效果处理。
 *
 * <p>事件点在 {@code LivingEntity.completeUsingItem()} 中触发，此时 {@code finishUsingItem} 已执行
 * （食物已被吃掉、饱和度已结算），因此这里追加的效果不会与进食逻辑冲突。
 */
@EventBusSubscriber(modid = Athland.MODID)
public final class AthlandItemEvents {
    private AthlandItemEvents() {}

    /**
     * 食用带 {@code athland:oxygen_restore} 的物品时，按组件数值补回空气槽。
     *
     * <p>只在服务端生效：空气槽经 SynchedEntityData 同步，客户端写入会被服务端覆盖。
     */
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        OxygenRestore restore = event.getItem().get(AthlandComponents.OXYGEN_RESTORE.get());
        if (restore == null) {
            return;
        }

        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        player.setAirSupply(Math.min(player.getMaxAirSupply(), player.getAirSupply() + restore.ticks()));
    }
}
