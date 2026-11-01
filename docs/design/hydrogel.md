# 渔货-水凝胶（hydrogel）

## 1. 背景

原版 1.21.1 没有「补充空气」的消耗品，潜水只能靠水下呼吸附魔或金苹果硬抗。本功能补上一个可重复获取的应急道具：钓鱼获得的渔货「水凝胶」，食用后补回一部分氧气。承载方向为**材料**（见 `docs/design/style.md` §1）。

## 2. 目标 / 非目标

**目标**
1. 新物品 `athland:hydrogel`，钓鱼可获得。
2. 食用后按固定量补充玩家氧气（空气槽）。
3. 补充量数据驱动（存在 Data Component 上），改数值不动逻辑。

**非目标**：不做合成配方；不做氧气上限提升或持续免耗；不做方块/流体/装备形态；不引新依赖。

## 3. 需求与约束

| 项 | 值 | 来源 |
|---|---|---|
| MC / 加载器 / Java | 1.21.1 / NeoForge 21.1.251 / 21 | AGENTS.md §2 |
| 命名 | `athland:hydrogel` | AGENTS.md §2.5 |
| 效果载体 | Data Component（`persistent`） | AGENTS.md §5 |

## 4. 设计思路

### 4.1 机制：氧气 = 空气槽

1.21.1 玩家的「氧气」就是 `Entity` 的空气槽：`getAirSupply()/setAirSupply(int)` 当前值，`getMaxAirSupply()` 上限（基类 300 tick = 15 秒）。水下每 tick −1，出水每 tick +4；空气槽经 `SynchedEntityData` 同步，客户端 HUD 自动更新。

「补充一定氧气」= 一次性加上固定量 N：

```
setAirSupply(min(getMaxAirSupply(), getAirSupply() + N))
```

**N = 150**（7.5 秒 = 上限一半）。

- **不回满（300）**：回满等于零成本永不溺水，会让水下呼吸附魔失去价值。半数槽定位为「救一次濒死下潜」，一次完整下潜需两个，形成补给节奏。
- **不用 `generic.oxygen_bonus`**：该属性是「降低氧气消耗速度」（等价水下呼吸），语义是减耗不是补充。

### 4.2 组件与物品

`athland:oxygen_restore` → `record OxygenRestore(int ticks)`（只提供 `Codec`）。

- 注册：`DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID)`。
- 只 `persistent`：值只在服务端读（空气槽本身已同步），不需要额外编码。
- `Item.Properties.component(...)` 必须在 `ITEMS.register(...)` 的 supplier 内部构造（静态初始化期取组件实例会 NPE）。

物品本体是普通 `Item`：`DataComponents.FOOD`（nutrition 2 / saturation 0.1 / `alwaysEdible`）+ `athland:oxygen_restore`。

### 4.3 食用时生效

监听 `LivingEntityUseItemEvent.Finish`（game bus）：事件点在 `finishUsingItem` 之后，食物已吃掉、饱和度已结算。处理器读 `event.getItem()` 上的组件，若是 `Player` 则补空气槽，**仅服务端执行**（空气槽由 SynchedEntityData 同步，客户端写会被覆盖）。

选事件而非覆写 `Item#finishUsingItem`：对「任何带该组件的食物」通用，后续新增带氧气的物品不用改代码。

### 4.4 钓鱼获得：Global Loot Modifier

钓鱼战利品走 `minecraft:gameplay/fishing`，按权重 85/10/5 派发到 `fish`/`junk`/`treasure`。原版表加载后冻结不可改写，`ItemFishedEvent.getDrops()` 又是副本 → 用 NeoForge 的 GLM。

- 自建子表 `athland:gameplay/fishing/hydrogel`（`type: minecraft:fishing`）：池内 `athland:hydrogel` 权重 5 + `minecraft:empty` 权重 95，每次钓鱼**额外 roll 一次** → 等价 5% 概率多一件水凝胶。
- GLM 条目 `type: neoforge:add_table`，`conditions` 用 `neoforge:loot_table_id` 指向 `minecraft:gameplay/fishing`，`table` 指向自建子表。GLM 只作用于顶层表（嵌套子表走 `getRandomItemsRaw` 不跑 modifier），所以每竿恰好多 1 件，原表 85/10/5 权重结构不变。
- `quality` 取 0 → luck 不影响概率（权重公式 `floor(weight + quality × luck)`），保持稳定预期。
- 5% 的定位：高于明显稀有物（treasure 每项 0.83%、热带鱼 1.7%）与普通 junk（1%），低于常见鱼（11%+）——不泛滥也不稀有到无用。

### 4.5 DataGen 基建

本项目原先没有任何 datagen 入口，本轮建立：

| Provider | 产出 |
|---|---|
| `LootTableProvider` | `data/athland/loot_table/gameplay/fishing/hydrogel.json` |
| `GlobalLootModifierProvider` | `data/athland/loot_modifiers/add_hydrogel_to_fishing.json` + `data/neoforge/loot_modifiers/global_loot_modifiers.json` |
| `ItemModelProvider` | `assets/athland/models/item/hydrogel.json` |
| `LanguageProvider` | `assets/athland/lang/en_us.json` |

入口 `AthlandDataProviders`：mod bus 上订阅 `GatherDataEvent`（`IModBusEvent`），`@EventBusSubscriber(modid=...)` + `static` 方法，`generator.addProvider(event.includeServer()/includeClient(), provider)`。

两条基建约束：①同一份资源只能由一处产出 —— 模板空占位 `src/main/resources/assets/athland/lang/en_us.json` 已删（与生成文件同路径会让 `processResources` 报 duplicate 失败）；②`build.gradle` 的 datagen 缓存排除写 `exclude(".cache/**")`。

**资源**：贴图 `assets/athland/textures/item/hydrogel.png`（16×16）用用户提供的美术库原图；style.md §4 美术规范未定稿的部分后续复核。

## 5. 文件规划

**新建**
```
src/main/java/com/miles/athland/component/OxygenRestore.java          record + Codec
src/main/java/com/miles/athland/component/AthlandComponents.java      DeferredRegister.DataComponents
src/main/java/com/miles/athland/item/AthlandItems.java                HYDROGEL 注册
src/main/java/com/miles/athland/event/AthlandItemEvents.java          食用后补氧
src/main/java/com/miles/athland/datagen/AthlandDataProviders.java     GatherDataEvent 入口
src/main/java/com/miles/athland/datagen/AthlandLootTableProvider.java 自建钓鱼子表
src/main/java/com/miles/athland/datagen/AthlandLootModifiers.java     GLM provider
src/main/java/com/miles/athland/datagen/AthlandItemModelProvider.java item model
src/main/java/com/miles/athland/datagen/AthlandLanguageProvider.java lang
src/main/resources/assets/athland/textures/item/hydrogel.png          贴图
```

**修改**：`src/main/java/com/miles/athland/Athland.java`（挂 ITEMS / DATA_COMPONENTS / 创意标签到 mod bus）；`build.gradle`（datagen 缓存排除）

**删除**：`src/main/resources/assets/athland/lang/en_us.json`（模板空占位，改由 DataGen 产出）

**生成（提交进仓库）**：`src/generated/resources/` 下 5 个文件 —— `data/athland/loot_modifiers/add_hydrogel_to_fishing.json`、`data/neoforge/loot_modifiers/global_loot_modifiers.json`、`data/athland/loot_table/gameplay/fishing/hydrogel.json`、`assets/athland/models/item/hydrogel.json`、`assets/athland/lang/en_us.json`

## 6. 验收标准

1. `.\gradlew.bat compileJava` / `build` 通过。
2. `.\gradlew.bat runData` 产出上述 5 个文件且内容正确。
3. 游戏内：钓鱼约 5% 获得 `athland:hydrogel`；`/give` 可直接获取。
4. 水下空气槽不足时食用，气泡立刻回涨 150 tick，HUD 同步更新。
5. 空气槽已满时食用不异常、不超过 `getMaxAirSupply()`；陆地食用不报错。
6. 生物食用不触发补氧。

## 7. 风险

| 风险 | 应对 |
|---|---|
| DataGen 基建首建，provider 写法有坑 | 只做本功能所需 4 个 provider，跑通再扩 |
| GLM 产出 `data/neoforge/loot_modifiers/global_loot_modifiers.json` 是跨 mod 共享路径 | 本项目是首个写 GLM 的 mod；第二个 mod 用 GLM 时需协调 `replace` 语义 |
| 贴图/中文名未按 style.md 定稿 | 本轮用美术库原图 + 英文名占位，规范定稿后替换 |

## 8. 决策记录

| 日期 | 决策 | 原因 |
|---|---|---|
| 2026-10-31 | 氧气机制 = `setAirSupply(current + N)` 一次性补固定量 | 需求是「补充氧气」；`generic.oxygen_bonus` 语义是减耗 |
| 2026-10-31 | N = 150 tick（上限 300 的一半） | 回满会让水下呼吸附魔失去价值；半数槽定位为「救一次濒死下潜」 |
| 2026-10-31 | 补充量存 `athland:oxygen_restore`（record 承载），仅 `persistent` | 数据驱动、可调；值只在服务端读 |
| 2026-10-31 | 钓鱼概率 5%（权重 5 / 空 95），`quality` = 0 | 落在稀有物之上、常见鱼之下；`quality=0` 使 luck 不影响 |
| 2026-10-31 | 钓鱼接入用 GLM（`neoforge:add_table` + `neoforge:loot_table_id`） | 原版表冻结不可改、`ItemFishedEvent` 拿的是副本 |
| 2026-10-31 | 效果挂在 `LivingEntityUseItemEvent.Finish`（game bus） | 事件点在 `eat()` 之后，对任意带该组件的食物通用 |
| 2026-10-31 | 本轮建立 DataGen 基建（4 provider + 入口） | AGENTS.md §5「DataGen 优先，不手写 JSON」 |
| 2026-10-31 | 删除手写空 `en_us.json`，语言完全交给 DataGen | 同路径两份文件会让 `processResources` 报 duplicate 失败 |
| 2026-10-31 | `build.gradle` 缓存排除改 `exclude(".cache/**")` | `PatternFilter` 相对每个源目录匹配，原写法匹配不到，缓存被打进 jar |

### 待确认

无。参数已全部落定（N=150、概率 5%、仅 persistent、luck 不影响）。

## 9. 参考 API（本地 `build/moddev/artifacts/neoforge-21.1.251-sources.jar`）

`Entity.getAirSupply/setAirSupply/getMaxAirSupply` · `LivingEntityUseItemEvent.Finish` · `DataComponentType.Builder` · `DeferredRegister.DataComponents` · `LootTableProvider`/`LootTableSubProvider` · `GlobalLootModifierProvider`/`AddTableLootModifier`/`LootTableIdCondition` · `GatherDataEvent` · `GameData.getRegistrationOrder`
