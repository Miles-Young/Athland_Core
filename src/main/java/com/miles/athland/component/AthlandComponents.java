package com.miles.athland.component;

import com.miles.athland.Athland;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Athland 的自定义 Data Component。
 *
 * <p>只有 {@link #OXYGEN_RESTORE} 一项：食用后一次性补回的空气槽刻数。
 * 仅 {@code persistent}，不同步到客户端 —— 效果由服务端执行，空气槽本身已由 SynchedEntityData 同步。
 */
public final class AthlandComponents {
    private AthlandComponents() {}

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Athland.MODID);

    /**
     * 物品携带的补氧量，单位 tick。作用于玩家，读取方见 {@code AthlandItemEvents}。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<OxygenRestore>> OXYGEN_RESTORE =
        DATA_COMPONENTS.registerComponentType("oxygen_restore",
            builder -> builder.persistent(OxygenRestore.CODEC));
}
