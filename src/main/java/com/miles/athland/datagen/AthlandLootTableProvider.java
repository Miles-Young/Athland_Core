package com.miles.athland.datagen;

import com.miles.athland.item.AthlandItems;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * 产出 Athland 自己的战利品表。
 *
 * <p>钓鱼子表 {@code athland:gameplay/fishing/hydrogel}：一次 roll，
 * 权重 5 给水凝胶、权重 95 给空条目，等价于每次钓鱼 5% 概率额外多一件水凝胶。
 */
public class AthlandLootTableProvider extends LootTableProvider {
    /** 自建钓鱼子表。由 {@link AthlandLootModifiers} 挂到原版钓鱼总表上。 */
    public static final ResourceKey<LootTable> HYDROGEL_FISHING =
        ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("athland", "gameplay/fishing/hydrogel"));

    public AthlandLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(HYDROGEL_FISHING),
            List.of(new SubProviderEntry(AthlandFishingLoot::new, LootContextParamSets.FISHING)),
            registries);
    }

    private record AthlandFishingLoot(HolderLookup.Provider registries) implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
            output.accept(
                HYDROGEL_FISHING,
                LootTable.lootTable()
                    .withPool(
                        LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(AthlandItems.HYDROGEL.get()).setWeight(5))
                            .add(EmptyLootItem.emptyItem().setWeight(95))
                    )
            );
        }
    }
}
