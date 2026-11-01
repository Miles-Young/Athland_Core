package com.miles.athland.datagen;

import com.miles.athland.item.AthlandItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * 产出 Athland 的物品模型。
 */
public class AthlandItemModelProvider extends ItemModelProvider {
    public AthlandItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(AthlandItems.HYDROGEL.get());
    }
}
