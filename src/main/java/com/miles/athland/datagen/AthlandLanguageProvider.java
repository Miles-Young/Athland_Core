package com.miles.athland.datagen;

import com.miles.athland.Athland;
import com.miles.athland.item.AthlandItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * 产出 Athland 的语言文件。
 *
 * <p>中文名待 style.md §5 命名规范补齐后再定；本轮先用英文名占位。
 */
public class AthlandLanguageProvider extends LanguageProvider {
    public AthlandLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {
        add(AthlandItems.HYDROGEL.get(), "Hydrogel");
        add("itemGroup." + Athland.MODID, "Athland");
    }
}
