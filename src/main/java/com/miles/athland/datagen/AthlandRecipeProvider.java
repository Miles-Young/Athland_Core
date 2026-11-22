package com.miles.athland.datagen;

import com.miles.athland.Athland;
import com.miles.athland.item.AthlandItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 产出 Athland 的配方。
 *
 * <p>「煎蛋」：原版鸡蛋经熔炉 / 烟熏炉 / 营火烹饪成 {@code athland:fried_egg}，
 * 三条线与原版熟肉（熟牛肉等）的配方结构完全同构。
 *
 * <p>「咸蛋」「皮蛋」：工作台无序合成，产出未开壳形态，需敲开去壳才能吃。
 *
 * <p>注意 1.21.1 的配方目录是单数 {@code data/<ns>/recipe/}；配方解锁进度由
 * {@link RecipeProvider} 自动生成到 {@code data/<ns>/advancement/recipes/}。
 */
public class AthlandRecipeProvider extends RecipeProvider {
    /** 烹饪经验，与原版肉类烹饪档一致。 */
    private static final float EXPERIENCE = 0.35F;
    /** 熔炉 / 烟熏炉烹饪时间（10 秒）。 */
    private static final int COOKING_TIME = 200;
    /** 营火烹饪时间（30 秒）。 */
    private static final int CAMPFIRE_COOKING_TIME = 600;

    public AthlandRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        Ingredient egg = Ingredient.of(Items.EGG);

        // 不做高炉（blasting）版本：原版食物一律没有高炉配方，高炉只处理矿物。
        SimpleCookingRecipeBuilder.smelting(egg, RecipeCategory.FOOD, AthlandItems.FRIED_EGG.get(), EXPERIENCE, COOKING_TIME)
            .unlockedBy("has_egg", has(Items.EGG))
            .save(recipeOutput, recipeId("fried_egg_from_smelting"));

        SimpleCookingRecipeBuilder.smoking(egg, RecipeCategory.FOOD, AthlandItems.FRIED_EGG.get(), EXPERIENCE, COOKING_TIME)
            .unlockedBy("has_egg", has(Items.EGG))
            .save(recipeOutput, recipeId("fried_egg_from_smoking"));

        SimpleCookingRecipeBuilder.campfireCooking(egg, RecipeCategory.FOOD, AthlandItems.FRIED_EGG.get(), EXPERIENCE, CAMPFIRE_COOKING_TIME)
            .unlockedBy("has_egg", has(Items.EGG))
            .save(recipeOutput, recipeId("fried_egg_from_campfire_cooking"));

        // 咸蛋：海带（咸味）+ 蛋。
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, AthlandItems.SALTED_EGG.get())
            .requires(Items.KELP)
            .requires(Items.EGG)
            .unlockedBy("has_egg", has(Items.EGG))
            .save(recipeOutput, recipeId("salted_egg"));

        // 皮蛋：下界石英 + 岩浆膏（碱化意象）+ 蛋。
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, AthlandItems.PIDAN.get())
            .requires(Items.QUARTZ)
            .requires(Items.MAGMA_CREAM)
            .requires(Items.EGG)
            .unlockedBy("has_egg", has(Items.EGG))
            .save(recipeOutput, recipeId("pidan"));
    }

    /**
     * 构造带本模组命名空间的配方 ID。
     *
     * <p>{@code RecipeBuilder.save(RecipeOutput, String)} 走 {@code ResourceLocation.parse}，
     * 不带冒号时会落到 {@code minecraft} 命名空间，因此这里必须显式给全限定 ID。
     */
    private static ResourceLocation recipeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(Athland.MODID, path);
    }
}
