package com.miles.athland.datagen;

import com.miles.athland.Athland;
import com.miles.athland.item.AthlandItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * 产出 Athland 的语言文件。
 *
 * <p>{@link LanguageProvider} 一个实例只写一个 locale 文件（{@code <locale>.json}），
 * 所以每种语言各注册一个实例。父类的 {@code locale} 字段是 private 且无访问器，
 * 这里自己留一份供翻译分支使用。
 *
 * <p>中文名待 style.md §5 命名规范补齐后再定；本轮先用直译名占位。
 */
public class AthlandLanguageProvider extends LanguageProvider {
    private final String locale;

    public AthlandLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        switch (locale) {
            case "zh_cn" -> {
                add(AthlandItems.HYDROGEL.get(), "水凝胶");
                add(AthlandItems.FRIED_EGG.get(), "煎蛋");
                add(AthlandItems.SALTED_EGG.get(), "咸蛋");
                add(AthlandItems.SALTED_EGG_OPENED.get(), "剥壳咸蛋");
                add(AthlandItems.PIDAN.get(), "皮蛋");
                add(AthlandItems.PIDAN_OPENED.get(), "剥壳皮蛋");
                add("itemGroup." + Athland.MODID, "艾瑟兰");
            }
            default -> {
                add(AthlandItems.HYDROGEL.get(), "Hydrogel");
                add(AthlandItems.FRIED_EGG.get(), "Fried Egg");
                add(AthlandItems.SALTED_EGG.get(), "Salted Egg");
                add(AthlandItems.SALTED_EGG_OPENED.get(), "Peeled Salted Egg");
                add(AthlandItems.PIDAN.get(), "Pidan");
                add(AthlandItems.PIDAN_OPENED.get(), "Peeled Pidan");
                add("itemGroup." + Athland.MODID, "Athland");
            }
        }
    }
}
