package com.miles.athland.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 物品携带的「补充氧气量」，单位 tick（20 tick = 1 秒）。
 *
 * @param ticks 一次性补回的空气槽刻数，实际生效量受 {@code Entity#getMaxAirSupply()} 约束
 */
public record OxygenRestore(int ticks) {
    public static final Codec<OxygenRestore> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.INT.fieldOf("ticks").forGetter(OxygenRestore::ticks)
        ).apply(instance, OxygenRestore::new)
    );
}
