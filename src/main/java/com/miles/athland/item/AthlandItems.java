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

    /**
     * 煎蛋：由原版鸡蛋烹饪（熔炉 / 烟熏炉 / 营火）得到的料理。
     *
     * <p>数值对标原版熟牛肉（nutrition 8 / saturationModifier 0.8），是本模组加工类食物的基线档。
     * 不加 {@code alwaysEdible}：与原版熟牛肉一致，饱食度满时不可食用。
     */
    public static final DeferredItem<Item> FRIED_EGG = ITEMS.register("fried_egg",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(8)
                .saturationModifier(0.8F)
                .build())));

    /**
     * 剥壳咸蛋：咸蛋敲开去壳后的形态，可直接食用。
     *
     * <p>数值与煎蛋同档（nutrition 8 / saturationModifier 0.8）。
     */
    public static final DeferredItem<Item> SALTED_EGG_OPENED = ITEMS.register("salted_egg_opened",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(8)
                .saturationModifier(0.8F)
                .build())));

    /**
     * 咸蛋：工作台用海带 + 蛋合成，需先敲开去壳才能吃。
     *
     * <p>刻意不带 FOOD 组件 —— {@link Item#use} 会因此不进入进食，敲开逻辑得以独占右键。
     */
    public static final DeferredItem<Item> SALTED_EGG = ITEMS.register("salted_egg",
        () -> new OpenableFoodItem(() -> SALTED_EGG_OPENED.get(), new Item.Properties()));

    /**
     * 剥壳皮蛋：皮蛋敲开去壳后的形态，可直接食用。
     *
     * <p>数值与煎蛋同档（nutrition 8 / saturationModifier 0.8）。
     */
    public static final DeferredItem<Item> PIDAN_OPENED = ITEMS.register("pidan_opened",
        () -> new Item(new Item.Properties()
            .food(new FoodProperties.Builder()
                .nutrition(8)
                .saturationModifier(0.8F)
                .build())));

    /**
     * 皮蛋：工作台用下界石英 + 岩浆膏 + 蛋合成，需先敲开去壳才能吃。
     *
     * <p>与咸蛋同理，未开壳形态不带 FOOD 组件。
     */
    public static final DeferredItem<Item> PIDAN = ITEMS.register("pidan",
        () -> new OpenableFoodItem(() -> PIDAN_OPENED.get(), new Item.Properties()));
}
