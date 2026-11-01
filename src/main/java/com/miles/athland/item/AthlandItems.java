package com.miles.athland.item;

import com.miles.athland.Athland;
import com.miles.athland.component.AthlandComponents;
import com.miles.athland.component.OxygenRestore;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Athland 的物品注册。
 */
public final class AthlandItems {
    private AthlandItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Athland.MODID);

    /**
     * 水凝胶：钓鱼可得的渔获，食用后补充 150 tick（7.5 秒）空气槽。
     */
    public static final DeferredItem<Item> HYDROGEL = ITEMS.register("hydrogel",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(2)
                .saturationModifier(0.1F)
                .alwaysEdible()
                .build())
            .component(AthlandComponents.OXYGEN_RESTORE.get(), new OxygenRestore(150))));
}
