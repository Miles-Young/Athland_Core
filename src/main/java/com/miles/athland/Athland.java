package com.miles.athland;

import com.mojang.logging.LogUtils;
import com.miles.athland.component.AthlandComponents;
import com.miles.athland.item.AthlandItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

/**
 * Athland 模组主类。
 *
 * <p>只负责把各注册表挂到 mod bus 上；具体内容分散在 {@link AthlandItems}、{@link AthlandComponents} 等类中。
 */
@Mod(Athland.MODID)
public class Athland {
    /** 模组 ID，须与 {@code neoforge.mods.toml} 中的条目一致。 */
    public static final String MODID = "athland";

    public static final Logger LOGGER = LogUtils.getLogger();

    /** 1.21.1 的创意标签只能用泛型 DeferredRegister 注册。 */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ATHLAND_TAB =
        CREATIVE_MODE_TABS.register("athland", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MODID))
            .icon(() -> new ItemStack(AthlandItems.HYDROGEL.get()))
            .displayItems((parameters, output) -> output.accept(AthlandItems.HYDROGEL.get()))
            .build());

    public Athland(IEventBus modEventBus) {
        AthlandComponents.DATA_COMPONENTS.register(modEventBus);
        AthlandItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
