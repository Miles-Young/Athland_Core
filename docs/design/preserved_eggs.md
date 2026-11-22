# 料理-咸蛋与皮蛋（salted_egg / pidan）

## 1. 背景

继「煎蛋」之后，艾瑟兰的食物库里再补两种蛋类料理：**咸蛋**（盐腌蛋）与**皮蛋**（又称百年蛋 / century egg）。两者都是「蛋经过特殊工艺处理」的食物，与原版只有「鸡蛋 → 熟肉」的加工链形成对照，补上原版缺失的**腌制 / 碱制**加工路线（见 `docs/design/style.md` §1「补完 + 扩展」）。

美术库已有 5 张成品图（16×16）：`蛋/煎蛋.png`、`蛋/咸蛋.png`、`蛋/咸蛋（打开）.png`、`蛋/皮蛋.png`、`蛋/皮蛋（打开）.png`。后两张「（打开）」图表示**敲开去壳后**的形态 —— 现实中咸蛋 / 皮蛋都要先敲开壳才能吃，视觉上蛋壳裂开、露出内部。

本轮新增两个料理，每个都有「未开壳」与「已开壳」两个形态。

## 2. 目标 / 非目标

**目标**
1. 新物品 `athland:salted_egg`（咸蛋）与 `athland:pidan`（皮蛋），可合成获得。
2. 每个都有「已开壳」形态：右键敲开 → 消耗未开壳的、产出已开壳的。
3. **必须先敲开才能吃**：未开壳形态不可食用；已开壳形态是普通食物。
4. 数值与煎蛋同档（nutrition 8 / saturationModifier 0.8），纯食物数值，不带效果。

**非目标**：不新增 Data Component（两态用两个独立物品承载）；不产生蛋壳物品；不引新依赖。

## 3. 需求与约束

| 项 | 值 | 来源 |
|---|---|---|
| MC / 加载器 / Java | 1.21.1 / NeoForge 21.1.251 / 21 | AGENTS.md §2 |
| 注册名 | `athland:salted_egg` / `athland:pidan`（含 `_opened` 变体） | AGENTS.md §2.5 |
| 贴图 | 16×16 PNG ×4，来源美术库原图 | — |
| 资源产出 | 一律 DataGen，不手写 JSON | AGENTS.md §5 |

## 4. 设计思路

### 4.1 为什么用两个独立物品而不是一个物品 + 组件

同一个物品要显示两张贴图，在 1.21.1 有两条路：

1. **一个物品 + Data Component 存状态**，模型用 `overrides` + `custom_model_data` 谓词切换贴图 —— 需要组件 + 模型 override + 客户端属性函数。
2. **两个独立物品**（`salted_egg` / `salted_egg_opened`），右键做物品转换。

选**方案 2**。理由：

- 方案 1 的 `custom_model_data` 是**通用物品属性**，`ItemProperties` 把它注册在 `GENERIC_PROPERTIES` 里（`ItemProperties.registerCustomModelData`），任何物品都能用它驱动 override —— 但这要求每个实例的 CMD 值唯一，属于借用原版给「同一物品多种模型变体」设计的通道，用它承载「开壳」这种玩法状态会与其他模组的 CMD 用法产生语义冲突。
- 方案 2 无组件、无客户端代码、无模型 override 复杂度，状态就是「手上是哪个物品」，存档里天然一致。
- 代价是注册表多两个名字（`salted_egg_opened` / `pidan_opened`）—— 可接受，四个物品总数仍很少。

### 4.2 物品与形态

| 注册名 | 中文 | 贴图 | 可食用 | 食物数值 |
|---|---|---|---|---|
| `athland:salted_egg` | 咸蛋 | `salted_egg.png` | ✗ | — |
| `athland:salted_egg_opened` | 剥壳咸蛋 | `salted_egg_opened.png` | ✓ | nutrition 8 / sat 0.8 |
| `athland:pidan` | 皮蛋 | `pidan.png` | ✗ | — |
| `athland:pidan_opened` | 剥壳皮蛋 | `pidan_opened.png` | ✓ | nutrition 8 / sat 0.8 |

- **未开壳形态不带 `DataComponents.FOOD`** → `Item.use()` 走 `foodproperties == null` 分支，不触发进食。敲开逻辑因此可以直接覆写 `use()`。
- **已开壳形态是普通 `Item` + `food(...)`** → 右键直接进食用原版流程，无需任何自定义逻辑。
- 数值取 `nutrition(8).saturationModifier(0.8F)`，与煎蛋、原版 `Foods.COOKED_BEEF` 一致；三个蛋类食物同档，横向可比。
- 不加 `alwaysEdible()`：与煎蛋一致，饱食度满时不可食用。

### 4.3 敲开：物品转换

未开壳形态需要一个自定义 `Item` 子类，覆写 `use(Level, Player, InteractionHand)`：

```
未开壳物品右键
  → 播放 SoundEvents.EGG_THROW（原版投掷鸡蛋的音效）
  → 服务端：消耗手中 1 个未开壳物品，产出 1 个对应的已开壳物品
  → 客户端：返回 InteractionResultHolder.sidedSuccess
```

- **服务端权威**：物品转换只在服务端执行。客户端 `use()` 返回原 stack（`itemstack1 == itemstack`），不触发 `MultiPlayerGameMode` 的客户端预测回写，由服务端权威同步。
- **转换用原版范式**：复用 `ItemUtils.createFilledResult(原stack, player, 新stack, preventDuplicates=false)` —— 与原版桶装液体（`BucketItem`）同一套逻辑。它一次处理掉几个边界：
  1. `emptyStack.consume(1, player)` —— 创造模式（`hasInfiniteMaterials()`）下不扣物品；
  2. 手上只剩最后一个时，剥壳蛋**直接顶到手上**（`emptyStack.isEmpty()` → 返回 filledStack），不进背包；
  3. 手上还有多个时，剥壳蛋进背包；背包满则 `player.drop(filledStack, false)` 掉在脚下。
  - **`preventDuplicates` 必须传 `false`**：三参重载默认 `true`，那是桶装液体的语义 —— 创造模式下「背包里已有同款物品就不再添加」，会让敲开蛋永远产不出新的剥壳蛋。传 `false` 后走正常路径，`Inventory.add` 在背包有空间时正常放入（`addResource` 返回剩余数 0 → `add` 返回 `true`，物品不会被销毁）。
- **产出落点**：由 `createFilledResult` 统一处理，不再手写 `Inventory.add` / `player.drop` 分支。
- **音效**：复用原版 `SoundEvents.EGG_THROW`（`entity.egg.throw`）—— 不用自定义 `SoundEvent`，省掉整套资源注册。播放参数照抄原版 `EggItem.use()`：`SoundSource.PLAYERS`、音量 `0.5F`、音高 `0.4F / (random.nextFloat() * 0.4F + 0.8F)`（带随机浮动，避免每次敲开音高完全相同）。`playSound(null, ...)` 传 `null` 玩家，客户端 `ClientLevel.playSeededSound` 的 `player == minecraft.player` 判断为假 → **只有服务端广播一次**，无重复播放。与原版投蛋一致。
- **无 Data Component**：转换靠物品 ID 本身表达，不存任何额外状态。

两个未开壳物品共用一个 Item 子类，通过构造参数指定「开壳后变成什么」，避免写两份几乎一样的 `use()`：

```java
new OpenableFoodItem(() -> AthlandItems.SALTED_EGG_OPENED.get(), new Item.Properties())
```

用 `Supplier` 延迟取 opened 物品，避开注册期序问题（`DeferredHolder.get()` 在 supplier 内部才安全）。

### 4.4 配方：工作台无序合成

| 配方文件 | 结果 | 材料 |
|---|---|---|
| `data/athland/recipe/salted_egg.json` | `athland:salted_egg` | `minecraft:kelp` + `minecraft:egg` |
| `data/athland/recipe/pidan.json` | `athland:pidan` | `minecraft:nether_quartz` + `minecraft:magma_cream` + `minecraft:egg` |

- **海带 → 咸蛋**：咸味来自海带（海带含碘、传统上用来腌咸菜），是最直觉的映射。
- **下界石英 + 岩浆膏 → 皮蛋**：皮蛋的传统工艺是**碱化**（草木灰 / 石灰水），MC 里没有碱，用「下界石英 + 岩浆膏」这套下界材料表达碱化意象。
- **无序合成**（`ShapelessRecipeBuilder`）：材料都是散件，无固定摆位必要；玩家不必记图案。
- 配方分类用 `RecipeCategory.FOOD`。
  > **注意**：1.21.1 的合成配方书没有「食物」页。`RecipeBuilder.determineBookCategory` 只把 `BUILDING_BLOCKS` / `TOOLS` / `COMBAT` / `REDSTONE` 映射到非 misc，`FOOD` 落到 `default → MISC`。所以生成的 JSON 里 `category` 是 `misc`，配方书里出现在「杂项」页 —— 原版的蛋糕、南瓜派同样是 `misc`。这是原版机制限制，非本模组可改。
  > 与煎蛋不同：煎蛋走 `SimpleCookingRecipeBuilder`，那套有独立的 `CookingBookCategory.FOOD`，因此进的是真正的「食物」页。

### 4.5 DataGen

复用既有基建（`AthlandDataProviders` / `AthlandRecipeProvider` / `AthlandItemModelProvider` / `AthlandLanguageProvider`），本轮无新增 provider：

| Provider | 本轮产出 |
|---|---|
| `AthlandRecipeProvider` | `data/athland/recipe/salted_egg.json`<br>`data/athland/recipe/pidan.json` |
| `AthlandItemModelProvider` | `assets/athland/models/item/{salted_egg,salted_egg_opened,pidan,pidan_opened}.json` |
| `AthlandLanguageProvider` | `en_us` + `zh_cn` 各增 4 条 |

- 配方用 `ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, result)`，`.requires(...)` 列材料，`.unlockedBy("has_egg", has(Items.EGG))` 给解锁条件（无解锁条件的配方会被 `RecipeBuilder.ensureValid` 拒绝）。
- 配方 ID 同样走 `recipeId()` 显式构造 `athland:` 全限定 —— `save(output, String)` 内部 `ResourceLocation.parse` 会把无冒号路径落到 `minecraft` 命名空间。

### 4.6 创意标签

`athland` 创意标签加入 4 个新物品。标签图标仍沿用水凝胶，不改。

## 5. 文件规划

**新建**
```
src/main/java/com/miles/athland/item/OpenableFoodItem.java     可敲开的未开壳物品
src/main/resources/assets/athland/textures/item/salted_egg.png         美术库原图
src/main/resources/assets/athland/textures/item/salted_egg_opened.png  美术库原图
src/main/resources/assets/athland/textures/item/pidan.png             美术库原图
src/main/resources/assets/athland/textures/item/pidan_opened.png      美术库原图
```

**修改**
```
src/main/java/com/miles/athland/item/AthlandItems.java        4 个物品注册
src/main/java/com/miles/athland/Athland.java                  创意标签增加 4 个物品
src/main/java/com/miles/athland/datagen/AthlandRecipeProvider.java   2 条配方
src/main/java/com/miles/athland/datagen/AthlandItemModelProvider.java 4 个模型
src/main/java/com/miles/athland/datagen/AthlandLanguageProvider.java 4 条 × 2 locale
README.md                                                当前内容表
```

**生成（提交进仓库，`src/generated/resources/`）**
```
data/athland/recipe/salted_egg.json
data/athland/recipe/pidan.json
data/athland/advancement/recipes/food/salted_egg.json
data/athland/advancement/recipes/food/pidan.json
assets/athland/models/item/salted_egg.json
assets/athland/models/item/salted_egg_opened.json
assets/athland/models/item/pidan.json
assets/athland/models/item/pidan_opened.json
assets/athland/lang/en_us.json（更新）
assets/athland/lang/zh_cn.json（更新）
```

## 6. 验收标准

1. `.\gradlew.bat compileJava` / `build` 通过。
2. `.\gradlew.bat runData` 产出上述文件，配方 JSON 的 `ingredients` / `result` 正确。
3. 游戏内：工作台用海带 + 蛋合成出 `athland:salted_egg`；用下界石英（`minecraft:quartz`）+ 岩浆膏 + 蛋合成出 `athland:pidan`。
4. 手持未开壳咸蛋右键 → 手上变成 `athland:salted_egg_opened`，贴图切换为「剥壳咸蛋」；手持 3 个时只消耗 1 个。
5. 敲开瞬间播放原版投掷鸡蛋音效（`entity.egg.throw`），音高有随机浮动。
6. 手持未开壳咸蛋**长按右键**不会进入进食动画（未开壳形态无 FOOD 组件）。
7. 手持已开壳咸蛋长按右键 → 正常进食，回复 4 格饱食度 + 12.8 饱和度。
8. 背包满时敲开，产出的已开壳物品掉在脚下而非消失。
9. 4 个物品都在创意标签内，贴图均正常显示（非紫黑）。

## 7. 风险

| 风险 | 应对 |
|---|---|
| 客户端预测导致物品复制 | 物品转换只在服务端执行；客户端 `use()` 返回原 stack，不触发客户端预测回写 |
| 手写 `shrink(1)` 在创造模式下真扣物品 | 改用 `ItemUtils.createFilledResult`，其内部走 `consume(1, player)`，创造模式不扣 |
| 手上只剩最后一个时剥壳蛋进背包而非留在手上 | 同上，`createFilledResult` 在 `emptyStack.isEmpty()` 时直接返回新 stack 顶到手上 |
| 创造模式下敲开产不出新的剥壳蛋 | `createFilledResult` 三参重载默认 `preventDuplicates=true`（桶装液体语义：背包已有同款就不再添加），必须显式传 `false` |
| `Item.use()` 默认走进食分支，未开壳形态必须没有 FOOD 组件 | 未开壳物品**不**调用 `.food(...)`；只有 `_opened` 变体带食物数值 |
| `DeferredHolder.get()` 注册期序 | `OpenableFoodItem` 接收 `Supplier`，在 `use()` 内才求值 |
| 蛋壳去向未定（丢弃 vs 回收） | 本轮直接丢弃，不产出蛋壳物品（已确认：不加蛋壳，避免复杂化） |
| 咸蛋 2 材料可在背包 2×2 格完成 | 与皮蛋统一归 `FOOD` 分类；不额外限制 |
| 合成配方书无「食物」页，配方落「杂项」页 | 原版机制限制（`determineBookCategory` 把 `FOOD` 映射到 `MISC`），原版蛋糕 / 南瓜派同样如此。烹饪类配方才有「食物」页 |
| 中文名未定稿（style.md §5 TODO） | 本轮用直译名占位：咸蛋 / 剥壳咸蛋 / 皮蛋 / 剥壳皮蛋 |

## 8. 决策记录

| 日期 | 决策 | 原因 |
|---|---|---|
| 2026-11-01 | 两态用**两个独立物品**承载，不用 Data Component + 模型 override | 避免借用 `custom_model_data` 通用属性承载玩法状态；省掉组件、客户端属性函数、模型 override 三处复杂度 |
| 2026-11-01 | 未开壳形态**不带** FOOD 组件，因此不可食用 | 「必须先敲开才能吃」的机制直接由组件有无表达，不需要额外拦截逻辑 |
| 2026-11-01 | 敲开 = 消耗 1 个未开壳 + 产出 1 个已开壳 | 与用户确认；`shrink(1)` 支持堆叠时逐个开壳 |
| 2026-11-01 | **修正**：转换改用原版 `ItemUtils.createFilledResult`，不手写 `shrink`/`Inventory.add`/`drop` 分支 | 复查发现手写版漏三个边界：①`shrink(1)` 在创造模式真扣物品（原版用 `consume` 判 `hasInfiniteMaterials()`）；②手上只剩最后一个时剥壳蛋应**直接顶到手上**而非进背包；③`ServerPlayerGameMode.useItem` 在 stack 为空时会 `setItemInHand(hand, EMPTY)`。`createFilledResult` 是原版 `BucketItem` 装液体的同一套逻辑，一次解决 |
| 2026-11-01 | **修正**：`createFilledResult` 显式传 `preventDuplicates=false` | 创造模式反馈「开蛋不会增加剥壳蛋数量」。根因：三参重载默认 `preventDuplicates=true`（桶装液体语义）→ 创造模式下 `contains(filledStack)` 为真便不添加，且永远返回原 stack。传 `false` 后走 `consume` + `Inventory.add` 正常路径 |
| 2026-11-01 | **修正**：`createFilledResult` 显式传 `preventDuplicates=false` | 创造模式反馈「开蛋不会增加剥壳蛋数量」。根因：三参重载默认 `preventDuplicates=true`（桶装液体语义）→ 创造模式下 `contains(filledStack)` 为真便不添加，且永远返回原 stack。传 `false` 后走 `consume` + `Inventory.add` 正常路径 |
| 2026-11-01 | 未开壳物品共用一个 `OpenableFoodItem` 子类，用 `Supplier` 指定开壳结果 | 避免两份重复的 `use()`；`Supplier` 规避注册期序 |
| 2026-11-01 | 数值与煎蛋同档（nutrition 8 / sat 0.8） | 三个蛋类食物统一基线档，便于横向比较与后续平衡 |
| 2026-11-01 | 无附加 buff | 与煎蛋保持一致，形成统一的「数值型料理」系列 |
| 2026-11-01 | 工作台无序合成，`RecipeCategory.FOOD` | 材料为散件无需固定摆位；成品语义是食物。注意合成配方书无「食物」页，实际落 `misc`（原版机制） |
| 2026-11-01 | 咸蛋 = 海带 + 蛋；皮蛋 = 下界石英 + 岩浆膏 + 蛋 | 用户给定配方；海带↔咸味、下界材料↔碱化工艺 |
| 2026-11-01 | 本轮不加自定义 `SoundEvent`，改用原版 `SoundEvents.EGG_THROW` | 人指定「用投掷鸡蛋的音效」；复用原版音效省掉整套资源注册，参数照抄 `EggItem.use()` |
| 2026-11-01 | 蛋壳直接丢弃，不产出蛋壳物品 | 人确认：不用弄蛋壳，避免复杂化 |
| 2026-11-01 | 已开壳形态中文名用「剥壳咸蛋」「剥壳皮蛋」 | 人给定命名 |

## 9. 参考 API（本地 `build/moddev/artifacts/neoforge-21.1.251-sources.jar`）

`Item.use(Level, Player, InteractionHand)` · `ItemUtils.createFilledResult` · `ItemStack.consume(int, LivingEntity)` · `InteractionResultHolder.sidedSuccess/consume` · `Player.getInventory().add(ItemStack)` · `Player.drop(ItemStack, boolean)` · `ShapelessRecipeBuilder.shapeless` · `ShapelessRecipe.canCraftInDimensions` · `RecipeCategory.FOOD` · `RecipeBuilder.determineBookCategory` · `RecipeBuilder.ensureValid` · `FoodProperties.Builder` · `DataComponents.FOOD` · `SoundEvents.EGG_THROW` · `ClientLevel.playSeededSound` · `MultiPlayerGameMode.useItem` · `ServerPlayerGameMode.useItem`
