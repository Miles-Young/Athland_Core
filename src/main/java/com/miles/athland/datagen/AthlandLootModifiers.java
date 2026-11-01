package com.miles.athland.datagen;

import com.miles.athland.Athland;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

/**
 * 产出钓鱼战利品相关的全局战利品修改器（GLM）。
 *
 * <p>用 {@code neoforge:add_table} 把自建子表挂到原版 {@code minecraft:gameplay/fishing} 上：
 * 原版表加载后冻结、条目列表不可安全改写，而 GLM 是 NeoForge 为此场景提供的官方机制。
 */
public class AthlandLootModifiers extends GlobalLootModifierProvider {
    /** 目标：原版钓鱼总表。 */
    private static final ResourceLocation VANILLA_FISHING =
        ResourceLocation.withDefaultNamespace("gameplay/fishing");

    public AthlandLootModifiers(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Athland.MODID);
    }

    @Override
    protected void start() {
        add("add_hydrogel_to_fishing",
            new AddTableLootModifier(
                new LootItemCondition[] {
                    LootTableIdCondition.builder(VANILLA_FISHING).build()
                },
                AthlandLootTableProvider.HYDROGEL_FISHING));
    }
}
