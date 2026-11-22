# 料理-煎蛋（fried_egg）

## 1. 背景

原版 1.21.1 没有「熟蛋」类食物：鸡蛋只能做蛋糕、南瓜派，不能作为正餐食材。本功能在艾瑟兰的食物链里补上一个加工类料理「煎蛋」——把原版鸡蛋烹饪成成品食物，承载方向为**材料**（见 `docs/design/style.md` §1）。

美术库已有成品图 `艾瑟兰的食物/蛋/煎蛋.png`（16×16），没有原料图 —— 原料直接用原版 `minecraft:egg`，不新增物品。

## 2. 目标 / 非目标

**目标**
1. 新物品 `athland:fried_egg`，可由原版鸡蛋烹饪获得。
2. 作为普通食物：食用回复饱食度，无额外机制。
3. 三种烹饪途径与原版熟肉一致：熔炉烧制 / 烟熏炉 / 营火烹饪。

**非目标**：不做生蛋物品；不做效果型食物（不挂 Data Component）；不做料理品类框架（tag / 品类分组）；不引新依赖。

## 3. 需求与约束

| 项 | 值 | 来源 |
|---|---|---|
| MC / 加载器 / Java | 1.21.1 / NeoForge 21.1.251 / 21 | AGENTS.md §2 |
| 注册名 | `athland:fried_egg` | AGENTS.md §2.5 |
| 贴图 | 16×16 PNG，来源美术库原图 | — |
| 资源产出 | 一律 DataGen，不手写 JSON | AGENTS.md §5 |
| 命名 / 中文名 | style.md §5 未定稿；本轮 en_us + zh_cn 双语，中文名用直译占位 | style.md §5 TODO |

## 4. 设计思路

### 4.1 物品本体

普通 `Item`，只带 `DataComponents.FOOD`：

```
nutrition(8) + saturationModifier(0.8)
```

**数值 = 原版 `Foods.COOKED_BEEF`（熟牛肉）**，`FoodData.eat()` 直接用 `nutrition` / `saturation`；`saturation` 由 `FoodConstants.saturationByModifier` 算出 = `8 × 0.8 × 2 = 12.8`。

- 对标「熟牛肉」而非「生牛肉」：熟牛肉是原版唯一「肉 → 熟肉」的加工基准档，煎蛋同为加工类蛋白食物，取同一档便于后续料理横向比较。
- **不加 `alwaysEdible()`**：与原版熟牛肉一致，饱食度满时不可食用，避免煎蛋变成无限回血/刷饱食的保底食物。
- 不加任何 `MobEffectInstance`：本轮定位为纯数值基线食物，效果型料理另开迭代。

### 4.2 配方：三种烹饪途径

原料 `minecraft:egg` → 成品 `athland:fried_egg`，与原版熟肉完全同构（熟牛肉同样有熔炉 / 烟熏炉 / 营火三条线）：

| 配方文件 | 途径 | 烹饪时间 | 经验 |
|---|---|---|---|
| `data/athland/recipe/fried_egg_from_smelting.json` | 熔炉 | 200 tick（10 s） | 0.35 |
| `data/athland/recipe/fried_egg_from_smoking.json` | 烟熏炉 | 200 tick（10 s） | 0.35 |
| `data/athland/recipe/fried_egg_from_campfire_cooking.json` | 营火 | 600 tick（30 s） | 0.35 |

- 烹饪时间与经验对齐原版 `cookRecipes` 的肉类档（熔炉/烟熏 200 tick、营火 600 tick、经验 0.35）。
- **不做高炉（blasting）**：原版食物一律没有高炉版本（高炉只处理矿物），煎蛋同理。
- 经验 0.35 与熟牛肉一致；鸡蛋获取成本低于牛肉（鸡 1～4 蛋），单蛋经验略高于原版比例，属有意接受（详见 §7 风险）。
- 成品带 `DataComponents.FOOD` → `SimpleCookingRecipeBuilder` 自动把配方归到 `CookingBookCategory.FOOD`，进配方书的「食物」页。

### 4.3 DataGen 基建扩展

本项目原先没有 `RecipeProvider`，本轮建立：

| Provider | 产出 |
|---|---|
| `AthlandRecipeProvider`（新建） | `data/athland/recipe/fried_egg_from_*.json`（3 个）<br>`data/athland/advancement/recipes/food/fried_egg_from_*.json`（3 个，配方解锁进度，自动生成） |
| `AthlandItemModelProvider`（改） | `assets/athland/models/item/fried_egg.json` |
| `AthlandLanguageProvider`（改） | `assets/athland/lang/en_us.json` 增加 `Fried Egg`<br>`assets/athland/lang/zh_cn.json` 增加「煎蛋」（双语） |

- `AthlandRecipeProvider` 继承 `net.minecraft.data.recipes.RecipeProvider`，覆写 `buildRecipes(RecipeOutput)`，构造参数 `(PackOutput, CompletableFuture<HolderLookup.Provider>)`。
- 每条配方必须 `.unlockedBy("has_egg", has(Items.EGG))` —— `SimpleCookingRecipeBuilder.ensureValid` 在没有解锁条件时直接抛异常。
- 配方进度由 `RecipeProvider` 自动生成，父进度是原版 `minecraft:recipes/root`（原版数据自带，无需自建）。
- 配方文件落在 `data/athland/recipe/`（1.21.1 目录名是单数 `recipe`），不是 `recipes/`。

### 4.4 创意标签

`athland` 创意标签的 `displayItems` 增加 `athland:fried_egg`；标签图标仍沿用水凝胶，不改。

## 5. 文件规划

**新建**
```
src/main/java/com/miles/athland/datagen/AthlandRecipeProvider.java   RecipeProvider
src/main/resources/assets/athland/textures/item/fried_egg.png        贴图（美术库原图）
```

**修改**
```
src/main/java/com/miles/athland/item/AthlandItems.java               FRIED_EGG 注册
src/main/java/com/miles/athland/Athland.java                         创意标签增加煎蛋
src/main/java/com/miles/athland/datagen/AthlandItemModelProvider.java 模型
src/main/java/com/miles/athland/datagen/AthlandLanguageProvider.java  英文名
src/main/java/com/miles/athland/datagen/AthlandDataProviders.java    挂 RecipeProvider
README.md                                                          当前内容表增加煎蛋
```

**生成（提交进仓库，`src/generated/resources/`）**
```
data/athland/recipe/fried_egg_from_smelting.json
data/athland/recipe/fried_egg_from_smoking.json
data/athland/recipe/fried_egg_from_campfire_cooking.json
data/athland/advancement/recipes/food/fried_egg_from_smelting.json
data/athland/advancement/recipes/food/fried_egg_from_smoking.json
data/athland/advancement/recipes/food/fried_egg_from_campfire_cooking.json
assets/athland/models/item/fried_egg.json
assets/athland/lang/en_us.json（更新）
assets/athland/lang/zh_cn.json（新建）
```

## 6. 验收标准

1. `.\gradlew.bat compileJava` / `build` 通过。
2. `.\gradlew.bat runData` 产出上述文件，且 `data/athland/recipe/` 下三个配方 JSON 内容正确（type / ingredient / result / cookingtime / experience / category）。
3. 游戏内：熔炉、烟熏炉、营火各放入一个原版鸡蛋，均能产出 `athland:fried_egg`。
4. 食用煎蛋回复 4 格饱食度（nutrition 8）与 12.8 饱和度；饱食度满时无法食用（无 alwaysEdible）。
5. 配方在配方书「食物」页可见，拿到蛋后自动解锁对应配方进度。
6. `/give @s athland:fried_egg` 可直接获取；物品在创意标签内。

## 7. 风险

| 风险 | 应对 |
|---|---|
| 单蛋 4 格饱食度偏强（鸡 1～4 蛋） | 有意按「对标熟牛肉」执行；若后续平衡不当，改 `FoodProperties` 数值即可，不动逻辑 |
| 经验 0.35/蛋，可能形成刷经验路径 | 与熟牛肉同档；若需要下调，只改 DataGen 里的 `experience` 参数 |
| 命名 / 中文名未定稿（style.md §5 TODO） | 本轮 en_us 用 `Fried Egg` 占位，规范定稿后替换 |
| 配方与美术库其他食物（番茄 / 小南瓜 / 混合种子）尚无品类框架 | 本轮单点落地，品类框架另开设计文档 |

## 8. 决策记录

| 日期 | 决策 | 原因 |
|---|---|---|
| 2026-11-01 | 原料用原版 `minecraft:egg`，不新增「艾瑟兰的蛋」物品 | 美术库只有成品图；新增原料物品要额外做贴图与获取链路，本轮不值 |
| 2026-11-01 | 食用效果 = 纯食物数值，不挂 Data Component | 效果型食物需要单独平衡设计，先立数值基线 |
| 2026-11-01 | 数值对标原版 `Foods.COOKED_BEEF`（nutrition 8 / saturationModifier 0.8） | 「对标熟牛肉」的准确落点；原版唯一「肉 → 熟肉」加工基准档 |
| 2026-11-01 | 不加 `alwaysEdible` | 与原版熟牛肉一致；避免煎蛋成为饱食度满时的万能食物 |
| 2026-11-01 | 三条烹饪配方（熔炉 / 烟熏 / 营火），无高炉 | 与原版熟肉的三条线完全同构；原版食物一律无高炉版本 |
| 2026-11-01 | 烹饪时间 200/200/600 tick，经验 0.35 | 对齐原版 `cookRecipes` 肉类档 |
| 2026-11-01 | 本轮新建 `AthlandRecipeProvider` | 项目原先无配方 DataGen，AGENTS.md §5「DataGen 优先，不手写 JSON」 |
| 2026-11-01 | 英文名占位 `Fried Egg` | style.md §5 命名规范未定稿，与水凝胶轮次保持一致 |
| 2026-11-01 | **补充**：同时生成 `zh_cn.json`，中文名「煎蛋」 | 人反馈「貌似没zh」→ 改为双语。`LanguageProvider` 一个实例只写一个 locale 文件，故注册两个 provider 实例（en_us / zh_cn）。中文名用直译占位，规范定稿后替换 |

## 9. 参考 API（本地 `build/moddev/artifacts/neoforge-21.1.251-sources.jar`）

`RecipeProvider` · `RecipeOutput` · `SimpleCookingRecipeBuilder`（`smelting` / `smoking` / `campfireCooking` / `ensureValid`）· `RecipeCategory.FOOD` · `CookingBookCategory` · `FoodProperties.Builder` · `Foods` · `FoodConstants.saturationByModifier` · `FoodData.eat` · `Item.Properties.food` · `Items.EGG` · `GatherDataEvent`
