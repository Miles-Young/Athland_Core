package com.miles.athland.datagen;

import com.miles.athland.Athland;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * DataGen 入口：mod bus 上订阅 {@link GatherDataEvent}，把各 provider 交给 {@code DataGenerator}。
 *
 * <p>{@code GatherDataEvent} 是 {@code IModBusEvent}，由 FML 逐 mod 派发；
 * 只有 mod id 在 {@code --mod} 列表里的 mod 才会真正执行其 generator。
 */
@EventBusSubscriber(modid = Athland.MODID)
public final class AthlandDataProviders {
    private AthlandDataProviders() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var registries = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();

        // 服务端资源：战利品表与全局战利品修改器
        generator.addProvider(event.includeServer(), new AthlandLootTableProvider(output, registries));
        generator.addProvider(event.includeServer(), new AthlandLootModifiers(output, registries));

        // 客户端资源：物品模型与语言文件
        generator.addProvider(event.includeClient(), new AthlandItemModelProvider(output, Athland.MODID, existingFileHelper));
        generator.addProvider(event.includeClient(), new AthlandLanguageProvider(output, Athland.MODID, "en_us"));
    }
}
