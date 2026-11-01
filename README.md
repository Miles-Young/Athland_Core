# Athland · 艾瑟兰大陆

> 冒险与探索主线，向原版未覆盖的领域拓展内容。

Athland 是一个**冒险拓展**向的 Minecraft 模组，核心体验是**新奇**——玩家能遇到、尝试、建造原版没有的东西。

新内容优先由**材料**与**装备**两个方向承载。与原版的关系是**补完 + 扩展**：

- **补完** —— 补上原版世界观与玩法链条的缺口。原版只有铁与金，缺铜、锡、银一类的新材料；原版只有小麦与胡萝卜，缺其他作物。补上之后，原版的玩法链条才算完整。
- **扩展** —— 原版已有这一类内容，但做得不够深、不够多，就在此之上加深。原版武器只有单一材质与单一升级线，可以扩展为多材质 + 锻打 / 附魔路线。

核心循环：**探索 → 采集新材料 → 打造新装备 → 变强后探索更深区域**。

---

## 当前内容

| 内容 | 说明 |
|---|---|
| 水凝胶（Hydrogel） | 钓鱼时约 5% 概率额外获得的渔货。食用后补回 150 tick（7.5 秒）空气槽，正好救回一次濒死下潜；不叠加、不超过上限。 |
| 创意标签 | 物品归入 `athland` 创意标签。 |

模组 ID `athland`，包名 `com.miles.athland`。数据生成（DataGen）基建已就位，资源由 `runData` 产出到 `src/generated/resources`。

## 环境要求

| 项 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| 加载器 | NeoForge 21.1.251 |
| Java | 21 |

## 构建

```bash
git clone https://github.com/Miles-Young/Athland_Core.git
cd Athland_Core
./gradlew build
```

产物输出到 `build/libs/athland-<version>.jar`。

## 开发命令

| 命令 | 用途 |
|---|---|
| `./gradlew compileJava` | 最小验证：只编译 |
| `./gradlew build` | 完整构建 + 测试 |
| `./gradlew runClient` | 启动客户端进游戏内验证 |
| `./gradlew runServer` | 启动服务端 |
| `./gradlew runData` | 跑数据生成，产出到 `src/generated/resources` |
| `./gradlew runGameTestServer` | 跑 GameTest |

源码目录 `src/main/java/com/miles/athland/`。若 IDE 提示依赖缺失，执行 `./gradlew --refresh-dependencies`。

## 文档

| 文件 | 内容 |
|---|---|
| [`docs/design/style.md`](docs/design/style.md) | 风格规范：玩法基调、世界观、美术、命名文本的唯一出处 |
| [`docs/design/hydrogel.md`](docs/design/hydrogel.md) | 水凝胶功能设计文档 |

风格条目未定稿处（如美术、命名规范）在 `style.md` 中标注为 TODO，后续功能设计以该文件为准。

## 许可

- 模组本体：All Rights Reserved（见 `gradle.properties` 的 `mod_license`）。
- 模板文件（`TEMPLATE_LICENSE.txt`）：MIT，版权归 NeoForged project 所有。
- 映射使用 Mojang 官方映射名称，版权归 Mojang 所有，许可全文见 <https://github.com/NeoForged/NeoForm/blob/main/Mojang.md>。

## 社区资源

- NeoForged 文档：<https://docs.neoforged.net/>
- NeoForged Discord：<https://discord.neoforged.net/>
