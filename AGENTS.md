# AGENTS.md — Athland（MC 1.21.1 NeoForge）
OpenCode + MiniMax M3.1 Flash

## 1. 协作
- 人：提理念/需求、拆任务、确认文档、最终审查、高风险确认。
- AI：写设计文档、按确认文档编码、查 API、分析崩溃日志。
- 不自主跑“设计→编码→测试→修复”长链路；拆步执行，每步等人确认。
- 非 trivial 编码：先出设计文档，人确认后再写代码。
- 回复直接给结论或结果，禁止寒暄、复述问题、空泛总结。

## 2. 版本锁死
Minecraft 仅 **1.21.1**；Java 21；加载器仅 **NeoForge 21.1.x**。版本以 `gradle.properties` 为准：`minecraft_version=1.21.1`、`neo_version=21.1.251`（没有 `neoForgeVersion` 这个键，别读错）；禁止混入 Fabric/Forge 代码或映射。
开工前读 `gradle.properties` 和 `build.gradle`；不符则停止询问，不猜。

## 2.5 模组风格
作者 miles；模组名 Athland；ID `athland`；包名 `com.miles.athland`；类名大驼峰；注册名小写下划线。
> 已迁移：源码目录 `src/main/java/com/miles/athland/`、`gradle.properties` 的 `mod_group_id` 两处已同步为 `com.miles.athland`。

面向 1.21.1 NeoForge 现代写法，不写旧版兼容分支。注释只解释“为什么”，不逐行翻译“是什么”。优先复用现有工具类/基类。
玩法/美术/世界观风格见 `docs/design/style.md`（**骨架已建，内容待人填写**；对应条目为空时向人确认风格，不要凭空臆造）。编码任务不默认读取。

## 3. 设计文档先行
- 路径 `docs/design/<功能名>.md`；流程：人提想法 → AI 写文档 → 人确认 → AI 编码。
- 未确认文档前不改源码；偏离文档先改文档并确认。
- 文档只写：背景、目标/非目标、需求与约束、设计思路（机制/交互/方块物品事件/数据存储/资源）、文件规划、验收标准、风险、决策记录。1～2 页，复杂功能拆子文档。
- 任何偏离 AGENTS.md 或设计文档的临时处理，必须在注释中标注 `// DEVIATION:` 原因及清理条件，并在提交说明中记录。

## 4. 不确定就给选项
以下必须停下确认，不自行假设：版本/加载器/映射/依赖不确定；多方案影响架构/兼容/性能/存档；API 存在性或签名不确定；文档歧义/遗漏/冲突；要引入新依赖/资源/改构建配置；涉及存档、网络、Data Component 等敏感设计。
给 **2～4 个选项**，写清做法、风险、推荐项；可推荐但不替人拍板。确认前不写代码、不引依赖，结果写入设计文档“决策记录”。

## 5. NeoForge 1.21.1 硬规则
以下签名以本地 `build/moddev/artifacts/neoforge-21.1.251-sources.jar`（含 MC + NeoForge 全部反编译源码）为准，动手前先核对。

- 注册用类型化 `DeferredRegister.Blocks` / `.Items`（`createBlocks` / `createItems`），挂 **mod bus**；不用泛型旧写法。唯一例外：创意标签 1.21.1 只有泛型 `DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)`。
- 注册自定义 `DataComponentType` 用 `DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID)`；`createDataComponents(String)` 已废弃待删，不用。
- mod bus：注册、`FMLCommonSetupEvent`、`BuildCreativeModeTabContentsEvent`；game bus：`NeoForge.EVENT_BUS`。
- 事件优先独立 `@EventBusSubscriber`，方法必须 `static`（FML 4.0.44 只扫 static）；不在构造函数里注册 `this`。`bus()` 在 1.21.1 已废弃且被忽略，`modid` 仅多 mod 包才需要。模板 `Athland.java` 目前是构造函数里注册 `this` + 非 static 订阅，与本规则冲突，**以本规则为准**，改模板需经人确认。
- 物品数据用 **Data Components**：1.21.1 的 `ItemStack` **没有** `getTag/setTag/getOrCreateTag` 等 NBT 存取 API；自定义 NBT 走 `DataComponents.CUSTOM_DATA`（`CustomData`）或自定义 `DataComponentType`。读写用 `ItemStack.get/getOrDefault/has/set/remove`。
- 自定义 `DataComponentType` **必须** `persistent(Codec)`：只给 `networkSynchronized(StreamCodec)` 的类型 `isTransient()==true`，落盘时 `DataComponentPatch.CODEC` 会**静默丢弃**该值（`DataComponentMap.CODEC` 则报 `not a persistent component`），等于没存。
- 1.21.1 的 `DataComponentType.Builder.build()` 在只给 `persistent(codec)` 时会**自动从 codec 派生 streamCodec**，所以 persistent-only 组件**照样会被同步到客户端**；`networkSynchronized(...)` 只是省掉派生、改用自定义二进制编码。（旧文档说「要客户端同步再叠加 networkSynchronized」是错的。）
- **注册时序**：`Item.Properties.component(...)` 里取 `DeferredHolder.get()` 必须发生在 `DeferredRegister.register(name, supplier)` 的 **supplier 内部**（注册期）。静态初始化期取组件实例 → `DeferredHolder` 未绑定 → NPE。
- 值优先 `record`（record 自带 equals/hashCode）；用普通类时才自己实现 `equals`/`hashCode`。
- BlockEntity 存读用 `saveAdditional(CompoundTag tag, HolderLookup.Provider registries)` / `loadAdditional(CompoundTag, HolderLookup.Provider)`。**1.21.1 没有 `ValueInput`/`ValueOutput`**（更晚版本才引入），写这两类必然编译不过。自定义持久数据优先用 `IBlockEntityExtension#getPersistentData()`。
- DataGen 优先，不手写 JSON；1.21.1 入口是 mod bus 上的 `GatherDataEvent`，无基础设施先问。
- **DataGen 基建**：`GatherDataEvent` 是 `IModBusEvent`，入口类用独立 `@EventBusSubscriber(modid=...)` + `static` 方法；`generator.addProvider(event.includeServer()/includeClient(), provider)`；取 `generator.getPackOutput()`、`event.getLookupProvider()`、`event.getExistingFileHelper()`。`--mod <id>` 时**只有该 mod 的 generator 会执行**，flat 输出到 `--output` 根目录；`src/generated/resources` 已由 `build.gradle` 的 `sourceSets.main.resources.srcDir` 纳入。
- **同一份资源只能由一处产出**：`src/main/resources` 与 `src/generated/resources` 出现同一相对路径 → `processResources` 直接报 duplicate 失败。删手写占位，别加 `duplicatesStrategy` 绕过。
- **Gradle `PatternFilter` 相对每个源目录匹配**：datagen 缓存排除写 `exclude(".cache/**")`，写 `src/generated/**/.cache` 匹配不到，缓存文件会被打进 jar。
- **GLM（全局战利品修改器）**：只作用于**顶层**表 —— 嵌套子表走 `LootTable.getRandomItemsRaw`，不跑 modifier；用 `neoforge:loot_table_id` 条件匹配顶层表 ID。`AddTableLootModifier` 的 `conditions` 是 `LootItemCondition[]`（键名 `conditions`），GLM 外层的 NeoForge 条件键名是 `neoforge:conditions`，两者不冲突。`GlobalLootModifierProvider` 固定额外写 `data/neoforge/loot_modifiers/global_loot_modifiers.json`（**跨 mod 共享路径**，第二个 mod 用 GLM 时要协调 `replace` 语义）。
- 元数据源文件是 `src/main/templates/META-INF/neoforge.mods.toml`（生成到 `build/generated/sources/modMetadata/`，别改生成物）。**minecraft 依赖**范围写死小版本区间，如 `[1.21.1]`（与 `minecraft_version_range` 一致）；neoforge / loader 依赖按上游惯例保留开放上界（如 `[21.1.251,)`、`[1,)`），别一刀切封顶。
- `if/for/while` 带花括号；注册表不手写裸 ID；CompoundTag / Component 读写显式处理 null 与缺失字段。
- 查 NeoForge/MC 源码、签名或 diff：解包本地 `build/moddev/artifacts/neoforge-21.1.251-sources.jar`，命名是 Mojang 名 + Parchment 参数名（`build.gradle` 配了 Parchment `2024.11.17`）；需上游对照再 webfetch NeoForge GitHub 对应分支。本环境**没有** `minecraft-dev` skill（只有 `opencode`、`report`）；MC 专用 prompt 来自 `mccode-plugin`（`/new`、`/mcstatus` 命令），那不是 skill。

## 6. 请求格式
```
【任务】一句话。
【文件】只列要新建/修改的文件。
【约束】版本、加载器、API 限制。
【完成标准】可验证条件。
【测试要求】默认最小验证 / 指定游戏内验证 / 指定边界场景（可选）
【禁止】范围外操作。
```
一次只做一个功能模块。

## 7. 调试、测试与安全
- 崩溃：给完整日志 + 相关代码；先定位原因并解释，人确认后再修；不猜着修。
- 编码完成后不自行跑测试，先给测试选项表（最小验证 `compileJava`/`build`、游戏内 `runClient`/`runServer`、datagen `runData`、边界回归、自动化测试），标明执行方式、成本、推荐项；人选定后再执行。默认至少给“最小验证 + 游戏内验证步骤”。
- 测试命令不自动执行；先展示命令和预期结果，等人确认。
- 以下先展示命令/改动，等人确认：`gradle build` / `runClient`、改构建或依赖、删除/重命名源文件、写资源、`git commit/push`。
- 当前目录还不是 git 仓库（无 `.git`）；需要版本控制时先问人是否 `git init`。
- 不处理含真实 API Key、令牌或未公开专有代码的文件。

## 8. 一句话准则
先出简洁设计文档再写代码；只写 1.21.1 NeoForge API；注册挂 mod bus；物品用 Data Components（1.21.1 的 `ItemStack` 无 NBT 存取）；BlockEntity 用 `saveAdditional/loadAdditional(CompoundTag, HolderLookup.Provider)`（无 Value I/O）；事件用独立 `@EventBusSubscriber` + static；DataGen 优先；minecraft 依赖范围写死；查源码用本地 sources jar + Parchment/mojmaps 命名；不确定给选项等确认，不猜“应该能用”。
