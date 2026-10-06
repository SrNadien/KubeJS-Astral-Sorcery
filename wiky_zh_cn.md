# Astral Sorcery KJS

[English](wiky.md) · [Español](wiky_es.md) · **中文**

为 **Astral Sorcery 2.0**（Minecraft 1.21.1，NeoForge）提供 KubeJS 支持。装上本附属后，你可以：

- 用脚本创建星辉魔法的每一种配方类型，并支持链式方法；
- 一次删除一条或一批配方，并使用星辉魔法专属的新过滤器；
- 注册**新星座**；
- 添加**新的祭坛层级**——既可以是在现有层级上新建一座祭坛，也可以是真正的第五、第六……层级；
- 添加、修改和删除**宝典条目**；
- 修改**流明绑定**、**星辉魔法配置**与**降星点**效果的数值。

前置要求：KubeJS `2101.7.2` 或更高，Astral Sorcery `2.0.1` 或更高。

每个功能的可用脚本都在 [`examples/`](examples/) 里：把 `startup_scripts` 和 `server_scripts` 复制进你的 `kubejs` 文件夹即可。

> 星辉魔法 2.0 是一次彻底重写。它没有仪式基座：各星座的效果如今全在
> **流明绑定**之中，也就是下文「流明绑定」一节所修改的内容。

## 目录

| 功能 | 入口 | 脚本类型 |
|---|---|---|
| [配方](#配方) | `AstralSorcery.recipes` | server |
| [移除配方](#移除配方) | `event.remove({...})` | server |
| [星座](#星座) | `AstralSorcery.registry('constellations', ...)` | startup |
| [祭坛层级](#祭坛层级) | `StartupEvents.registry('block', ...)` + `AstralSorcery.altarTiers` | startup |
| [宝典条目](#宝典条目) | `AstralSorcery.research` | server |
| [流明绑定](#流明绑定) | `AstralSorcery.lumenBindings` | server |
| [配置项](#配置项) | `AstralSorcery.config` | server |
| [降星点](#降星点) | `AstralSorcery.focalPoint` | server |

星座、流明与祭坛效果的 id 都可以省略命名空间：`'aevitas'` 等同于
`'astralsorcery:aevitas'`。颜色支持 `0x7F5FFF`、`'#7F5FFF'` 或 `'#FF7F5FFF'`。

---

## 配方

在 `AstralSorcery.recipes(event => { ... })` 里写星辉魔法配方。该事件为每种配方类型提供一个函数，
另外还有常规的 `remove`、`replaceInput`、`replaceOutput`、`forEachRecipe` 和 `findRecipes`。
同样的类型在 `ServerEvents.recipes` 里也能用，写作 `event.recipes.astralsorcery.<type>`，或使用下列别名：

| 配方类型 | 在 `AstralSorcery.recipes` 中 | 在 `ServerEvents.recipes` 中 |
|---|---|---|
| 祭坛合成 | `event.altar_crafting` 或 `event.altar` | `event.recipes.astralAltar` |
| 星能物品嬗变 | `event.focal_combine` 或 `event.combine` | `event.recipes.astralCombine` |
| 星能方块嬗变 | `event.focal_transmutation` 或 `event.transmutation` | `event.recipes.astralTransmutation` |
| 流明生成 | `event.lumen_generation` 或 `event.lumen` | `event.recipes.astralLumen` |
| 流明结晶 | `event.lumen_crystallization` 或 `event.crystallization` | `event.recipes.astralCrystallization` |
| 聚星缸 | `event.lightwell` | `event.recipes.astralLightwell` |
| 星能注入 | `event.infusion` | `event.recipes.astralInfusion` |
| 星能液 | `event.liquid_starlight` | `event.recipes.astralLiquidStarlight` |
| 圣杯流体交互 | `event.liquid_interaction` | `event.recipes.astralLiquidInteraction` |

函数只要求填入配方缺了就做不成的参数。其余一切都用链式方法设置，
并且每条配方都接受 `.id('pack:name')`。

### 祭坛合成

```js
event.altar_crafting(output, pattern, key)
```

- `output`：一个物品，或一个物品列表。
- `pattern`：最多 3 行、每行最多 3 个字符；空格代表空槽位。过短的图案会自动补齐。
- `key`：字符到原料的映射。某个字符也可以写成流体容器要求：
  `{ fluid: 'astralsorcery:liquid_starlight', amount: 1000 }`。

| 方法 | 含义 | 默认值 |
|---|---|---|
| `.type(t)` | 所需祭坛层级：`illumination`、`resonance`、`luminance`、`radiance`，或某个[真正的层级](#真正的层级) | `illumination` |
| `.relay(pattern)` | 祭坛周围聚星转继台的 5×5 图案；中心必须留空。使用同一套 key | 空 |
| `.define(char, ingredient)` | 添加或替换一个 key | |
| `.constellation(c)` | 祭坛中需要放入的共鸣水晶石（`.focus(c)` 同样可用） | 无 |
| `.shatterChance(f)` | 定焦物碎裂的概率 | `0` |
| `.duration(ticks)` | 合成耗时（最低 20） | `100` |
| `.anyTime()` / `.night()` / `.onlyNight(bool)` | 是否只能在夜间 | 仅夜间 |
| `.chain()` / `.mayChain(bool)` | 原料仍足够时连续合成 | `false` |
| `.starlight(...constellations)` | 必须由这些星座的星能照射 | |
| `.lumen(lumen, amount)` | 消耗的流明 | |
| `.fluid(fluid, amount)` | 消耗的流体（来自放在附近的容器中的液体） | |
| `.additional('4x minecraft:redstone')` | 从附近额外消耗的物品 | |
| `.effect(id)` / `.noEffects()` | 视觉效果。默认值随层级而定，与星辉魔法自带配方一致 | |
| `.altar('kubejs:my_altar')` | 只有该祭坛方块能合成此配方，见[祭坛层级](#祭坛层级) | |

产物修饰：

| 方法 | 效果 |
|---|---|
| `.upgradesTo(block)` / `.setBlock(block)` | 用方块替换祭坛，而不是掉落物品（祭坛升级） |
| `.researchTier(tier)` | 提升合成者的研究层级 |
| `.replaceWithInput(slot)` / `.replaceWithInput('relay', slot)` | 产物变成该输入的副本 |
| `.copyComponents('grid', slot)` / `.copyComponents('grid', slot, [components])` | 从输入复制数据组件 |
| `.setComponent(type, value)` | 在产物上设置数据组件 |
| `.increaseEnchantments(chance)` | |
| `.gemModifier()`、`.enchantmentModifier()`、`.generateIdentifier()`、`.mergeCrystalProperties()` | |
| `.flag('is_artifact_enhanced')` | |
| `.artifactShardLoot(min, max)` | |
| `.crystalCount(perSize)` | |
| `.modifier({ type: '...', ... })` | 其他任意修饰，按原始 JSON 写 |

```js
AstralSorcery.recipes(event => {
  event.altar_crafting('2x minecraft:emerald', ['A A', ' B ', 'A A'], {
    A: 'minecraft:gold_ingot',
    B: 'astralsorcery:resonating_gem'
  })
  .type('radiance')
  .relay(['R   R', '     ', '     ', '     ', 'R   R'])
  .define('R', 'astralsorcery:stardust')
  .constellation('aevitas')
  .starlight('aevitas', 'armara')
  .lumen('prismatic', 200)
  .fluid('astralsorcery:liquid_starlight', 1000)
  .additional('4x minecraft:redstone')
  .anyTime()
  .duration(300)
  .id('mypack:altar/emeralds')
})
```

### 星能物品嬗变

丢进降星点光柱中的物品会被合并。

```js
event.focal_combine(outputs, inputs)
```

方法：`.input(ingredient)`、`.output(item)`、`.duration(ticks)`（100）、`.color(color)`、`.constellation(c)`。

### 星能方块嬗变

降星点会把光柱下方的方块变成其他方块。

```js
event.focal_transmutation(outputBlocks, inputs)
```

- `outputBlocks`：带状态的方块 id（`'minecraft:cake[bites=2]'`），或它们的列表；会随机选中其中一个。
- `inputs`：方块 id、`'#tag'`，或任意原版方块谓词 JSON。只要**任意一个**输入匹配，配方就会生效。

方法：`.state(block)` / `.state(block, weight)`（额外产物，权重默认 1）、`.input(predicate)`、`.display(ingredient)`（由普通方块输入自动填充）、`.duration(ticks)`（100）、`.color(color)`、`.constellation(c)`、`.focused(true)`（需要来自聚焦水晶石光柱的星能）。

### 流明生成

```js
event.lumen_generation(lumen, catalyst)
```

方法：`.amount(n)`（1）、`.attemptMultiplier(f)`（1）、`.starlightConsumption(f)`（1）、`.shatterMultiplier(f)`（1）、`.combine(lumen, amount)`。带 `.combine(...)` 的配方在**流明炼金阵列**中制作，不带则在普通流明阵列中制作。不要让两条流明生成配方共用同一种催化剂。

### 流明结晶

```js
event.lumen_crystallization(lumen, catalyst)
  .lumenPerOperation(900)   // 1-1900，默认 1400
  .shatterMultiplier(3)     // 默认 1，0 = 永不碎裂
```

### 聚星缸

```js
event.lightwell(fluid, catalyst)
  .productionMultiplier(1.2) // 默认 0.5
  .shatterMultiplier(0.2)    // 默认 10
  .color('#88CCFF')
```

### 星能注入

```js
event.infusion(output, input)
```

方法：`.fluid(fluid)`（默认 `astralsorcery:liquid_starlight`）、`.duration(ticks)`（200）、`.consumptionChance(f)`（0.05）、`.multipleFluids(bool)`、`.chalice(bool)`（true）。

### 星能液

丢进一池星能液中的物品。

```js
event.liquid_starlight(outputs, input)
```

`outputs` 是一个物品（直接掉落），或一组产物修饰；与下列方法之一搭配时可写 `[]`。

方法：`.with(ingredient)`（另一件必须也在池中的物品）、`.drop(item)`、`.duration(ticks)`（60）、`.randomDuration(ticks)`（20）、`.color(c)`、`.consumesLiquid(bool)`（false）、`.consumesInputs(bool)`（true）、`.mergeCrystal()`、`.formCrystalCluster()`、`.formGemCrystalCluster()`、`.growSize()`、`.bindLumen()`、`.fillLumen()`。

```js
event.liquid_starlight('minecraft:amethyst_shard', '2x minecraft:quartz')
  .with('minecraft:glowstone_dust')
  .consumesLiquid(true)
```

### 圣杯流体交互

两种流体在纳星圣杯中相遇。

```js
event.liquid_interaction(result, fluidA, fluidB)
  .chanceA(0.3).chanceB(1).weight(5)

event.liquid_interaction('minecraft:dirt', Fluid.of('minecraft:lava', 5), Fluid.of('minecraft:water', 5))
  .spawnEntity('minecraft:blaze')
```

### 替换输入与输出

`event.replaceInput` 与 `event.replaceOutput` 对所有星辉魔法配方都有效，包括祭坛合成格的键位、转继台槽位、额外输入与星能液掉落物。

```js
event.replaceInput({ astral_type: 'altar' }, 'astralsorcery:stardust', 'minecraft:glowstone_dust')
```

---

## 移除配方

所有 KubeJS 过滤器都能用（`id`、`type`、`mod`、`input`、`output`、列表、`not`、`or`）。本附属另外新增：

| 过滤器 | 匹配 |
|---|---|
| `astral_type` | 简称类型名：`altar`、`combine`、`transmutation`、`lumen`、`crystallization`、`lightwell`、`infusion`、`starlight`、`interaction` |
| `altar_tier` | 该层级的祭坛配方 |
| `altar` | 锁定到该祭坛方块的配方 |
| `constellation` | 任何用到该星座的配方（定焦物、星能、必需星座） |
| `focus` | 以该星座为定焦物的祭坛配方 |
| `lumen` | 任何产出或消耗该流明的配方 |
| `fluid` | 任何用到该流体的配方 |
| `output_block` | 放置该方块的嬗变与祭坛升级 |
| `research_tier` | 会把玩家提升到该研究层级的祭坛配方 |

每个过滤器也接受列表，表示「其中任意一个」。

```js
event.remove({ id: 'astralsorcery:altar/illumination_wand' })                  // 一条配方
event.remove([{ id: 'astralsorcery:infusion/glass_lens' }, { id: 'astralsorcery:infusion/resonating_gem' }])
event.remove({ altar_tier: 'radiance', output: 'astralsorcery:dynamism_gem_sky' })
event.remove({ astral_type: 'lightwell', fluid: 'minecraft:lava' })
event.remove({ lumen: ['caldor', 'hyle'], astral_type: 'lumen' })
event.remove({ constellation: 'discidia', astral_type: 'combine' })
```

---

## 星座

写在 **startup** 脚本中：

```js
AstralSorcery.registry('constellations', event => {
  event.create('kubejs:lyra')
    .displayName('Lyra')
    .subtitle('The Harp of the Night')
    .description('Shown in the tome once the constellation is discovered.')
    .color('#7F5FFF')
    .major()
    .star(4, 5).star(12, 3).star(20, 9).star(26, 20).star(16, 27).star(6, 22)
    .chain(0, 1, 2, 3, 4, 5)
    .connect(5, 0)
    .focalPoint()
    .focusCrystal('unique_distances')
    .rootPerk('vicio', -45, 20)
    .rootPerkConnect('vicio_connect_in_1')
    .skyPosition(200, 45)
})
```

| 方法 | 含义 |
|---|---|
| `.color(c)` | 星座颜色 |
| `.major()` / `.minor()` / `.hidden()` | 次级星座只有到共鸣研究层级才能发现；隐藏星座永远无法发现 |
| `.star(x, y)`、`.stars([[x, y], ...])` | 0–31 网格上的星点，编号按添加顺序 |
| `.connect(a, b)`、`.connections([[a, b], ...])`、`.chain(a, b, c, ...)` | 星点之间的连线，按星点编号 |
| `.moonPhases('full', 'new', ...)` | 它出现在天空中的固定月相。不写则每个存档随机取 8 个月相中的 5 个 |
| `.seededMoonPhases(...)`、`.phaseDropOff(f)` | 随存档种子偏移的月相 |
| `.focalPoint()` | 该星座的降星点会在世界中生成，可用星盘找到 |
| `.focusCrystal(layout)` | 聚焦水晶石的星辰晶柱排布：`opposite_no_axis_symmetry`、`one_axis_symmetry`、`unique_distances`、`no_right_angles`、`no_dot_no_axis_symmetry`、`any` |
| `.focusSort('polygon' \| 'closest_first')` | 晶柱光束的绘制方式 |
| `.attunable()` | 玩家可以与之共鸣 |
| `.rootPerk(type, x, y)` | 在星能力图谱中创建它的根源星能力。`type` 决定经验获取方式：`aevitas`（放置方块）、`armara`（承受伤害）、`discidia`（造成伤害）、`evorsio`（破坏方块）、`vicio`（移动）。需要有空位 |
| `.rootPerkConnect(...perks)`、`.rootPerkModifier(attribute, mode, value)` | 把根源星能力接入图谱并给予加成（`mode`：0 加算，1 乘算基础值，2 乘算总值） |
| `.skyPosition(yaw, pitch)` | 它在天空中的绘制位置（yaw 0–359，pitch 10–80）。不写则自动添加一个 |
| `.subtitle(text)`、`.description(text)` | 宝典文本 |

需要知道的几件事：

- **玩家的共鸣祭坛**通过把聚星转继台摆在 `(x/2 − 7, y/2 − 7)` 来识别星座。避免使用会挤到同一格、或落在祭坛上（x 与 y 为 14–15）的星点布局，也避免使用已是现有星座一部分的布局。
- 想给它一个**流明**，就添加一条带 `.constellation('kubejs:lyra')` 的 `focal_combine` 或 `focal_transmutation` 配方来产出一种花，再为该花添加一条 `lumen_generation` 配方。
- `StartupEvents.registry('astralsorcery:constellations', ...)` 同样可用，方法一致。
- **不要从已经用过某个星座的存档中删除它。** 星辉魔法无法加载引用了缺失星座的玩家进度，会把进度重置。

---

## 祭坛层级

添加祭坛层级有两种做法。

### 现有层级上的新祭坛

一个新的祭坛方块，沿用现有层级的合成格、界面、音效与研究层级，但拥有**自己的多方块结构**，以及可选的**自己专属的配方**。

```js
StartupEvents.registry('block', event => {
  event.create('kubejs:celestial_altar', 'astralsorcery:altar')
    .displayName('Celestial Altar')
    .baseTier('luminance')
    .exclusive()
    .structureBlock(0, -1, 0, 'minecraft:gold_block')
})
```

### 真正的层级

一个真正的新层级，位于辉耀之上。配方可以要求它（`.type('stellar')`），它能合成所有更低层级的配方，并且在星芒宝典与 JEI 中以所基于层级的合成格显示。在 startup 脚本中声明它，**要写在任何注册事件之外**，然后创建它的方块：

```js
AstralSorcery.altarTiers(event => {
  event.create('stellar')
    .base('radiance')              // 界面、合成格、外形与音效
    .researchTier('radiance')      // 玩家获得该祭坛时给予的研究层级
    .block('kubejs:stellar_altar')
})

StartupEvents.registry('block', event => {
  event.create('kubejs:stellar_altar', 'astralsorcery:altar')
    .displayName('Stellar Altar')
    .baseTier('radiance')
    .realTier('stellar')
    .structureLayer(-1, [
      'D   D',
      '     ',
      '  D  ',
      '     ',
      'D   D'
    ], { D: 'minecraft:diamond_block' })
})
```

### 祭坛方块方法

| 方法 | 含义 |
|---|---|
| `.baseTier(t)` | `illumination`、`resonance`、`luminance` 或 `radiance`。同时决定模型 |
| `.realTier(name)` | 该方块是某个真正层级的祭坛 |
| `.exclusive()` | 该祭坛只合成锁定到它的配方 |
| `.structureFrom(t)` | 以 `none`、`resonance`、`resonance_expanded`、`luminance` 或 `radiance` 的结构为起点。默认：所基于的层级 |
| `.noBaseStructure()` | 只用你自己添加的方块 |
| `.structureBlock(x, y, z, block)` | 要求祭坛相对坐标处有某个方块 |
| `.structureLayer(y, rows, key)` | 要求某一层方块；rows 以祭坛为中心，空格 = 任意方块，`_` = 空气 |

结构中的方块可以写成方块 id、方块状态（`'minecraft:furnace[lit=true]'`）、标签（`'#minecraft:logs'`）、多个 id（`'minecraft:stone|minecraft:andesite'`）、`'air'`，或用 `'*'` 取消基础结构中的某项要求。其他所有 KubeJS 方块方法（硬度、模型、材质……）也都能用。

### 为你的祭坛添加配方

```js
event.altar_crafting('minecraft:nether_star', ['GGG', 'GDG', 'GGG'], { G: 'minecraft:gold_ingot', D: 'minecraft:diamond' })
  .type('luminance')
  .altar('kubejs:celestial_altar')   // 只有这个方块能合成

event.altar_crafting('minecraft:elytra', ['FFF', 'FDF', 'FFF'], { F: 'minecraft:feather', D: 'minecraft:diamond' })
  .type('stellar')                   // 真正的层级
```

想让玩家把它造出来，就在低一级的层级上添加一条带 `.upgradesTo('kubejs:stellar_altar')` 的祭坛配方。
共振星杖会照常显示结构预览，星芒宝典也可以用 `.structure('kubejs:stellar_altar')` 展示它。

---

## 宝典条目

写在 **server** 脚本中。条目会随 `/reload` 重新加载，并发送给每一位玩家。

```js
AstralSorcery.research(event => {
  event.create('kubejs:starry_diamond')
    .title('Starry Diamond')
    .tier('shimmer')
    .position(0, 3)
    .icon('minecraft:diamond')
    .lookup('minecraft:diamond')
    .connect('astralsorcery:welcome')
    .text('A diamond shaped by the stars.\\nThis is a new paragraph.')
    .recipe('mypack:altar/starry_diamond')
    .constellation('aevitas')
    .structure('astralsorcery:altar_t2')

  event.modify('astralsorcery:welcome', node => {
    node.text('<color="#55FFFF">One more page.</color>')
  })

  event.remove('astralsorcery:chisel')
})
```

| 方法 | 含义 |
|---|---|
| `.title(text)` | 宝典中显示的名称 |
| `.tier(t)` | `shimmer`、`illumination`、`resonance`、`luminance`、`radiance`：所属的研究星云，以及何时可见 |
| `.position(x, y)` | 在该星云中的位置（现有条目的 x 大致在 −8…5，y 在 −9…4） |
| `.icon(items)`、`.lookup(items)` | 图标，以及会从配方页链接到本条目的物品 |
| `.connect(...ids)`、`.disconnect(...ids)` | 与同一层级其他条目之间的连线 |
| `.text(text)` / `.texts(...)` | 文本页。纯文本或语言键。`\\n` 开始新段落；`<color="#RRGGBB">`、`<bold>`、`<italic>` 可用。一页约能放下 20 行 |
| `.recipe(id)` / `.recipe(id, type)` / `.altarRecipe(id)` / `.craftingRecipe(id)` / `.infusionRecipe(id)` | 配方页 |
| `.constellation(id)` | 星座的双页展开 |
| `.structure(id, index)` | 3D 结构页 |
| `.lumen(lumen, ...slots)` | 流明绑定页 |
| `.emptyPage()`、`.insertText(index, text)`、`.removePage(index)`、`.clearPages()` | 页面编辑 |
| `.requiresConstellation(...ids)` | 在其中一个星座被发现前显示为锁定 |
| `.requiresFlag('has_obtained_artifact')` | 在该标记被设置前隐藏 |
| `.background(location, ...path)` | 边框材质 |

配方页可以展示祭坛、工作台、星能注入、聚星缸、降星点与流明配方。

---

## 流明绑定

写在 **server** 脚本中。改动随 `/reload` 生效。

```js
AstralSorcery.lumenBindings(event => {
  event.modify('aevitas', binding => {
    binding.potionDuration(100, 200)
    binding.slot('helmet', slot => slot.cost(5).chance(0.75))
    binding.scaleModifiers(2)
  })

  event.create('kubejs:haste')
    .potion('minecraft:haste', 400, 800, 1, 1)
    .slot('tool', slot => slot.usageType('block_break').cost(2).chance(0.5).modifier('block_break_speed', 1, 0.25))

  event.map('armara', 'kubejs:haste')   // 甲御流明现在绑定这个
  event.remove('discidia')
})
```

事件：`modify(id)`、`modify(id, fn)`、`modifyAll(fn)`、`create(id)`、`remove(...ids)`、`map(lumen, binding)`、`getMapping(lumen)`、`ids`。

绑定：`potion(effect, minTicks, maxTicks, minAmp, maxAmp)`、`potionDuration(min, max)`、`potionAmplifier(min, max)`、`hiddenPotion(...)`、`noPotion()`、`slot(name)`、`slot(name, fn)`、`allSlots(fn)`、`removeSlot(name)`、`scaleModifiers(f)`、`cost(n)`、`chance(f)`、`raw(json)`。

槽位：`helmet`、`chestplate`、`leggings`、`boots`、`melee_weapon`、`ranged_weapon`、`tool`。

槽位对象：`cost(n)`、`chance(f)`、`usageType(type)`、`usage(json)`、`usageValue(field, value)`、`effect(json)`、`addEffect(json)`、`effectValue(field, value)`、`modifier(attribute, mode, value)`、`modifierValue(attribute, value)`、`removeModifier(attribute)`、`scaleModifiers(f)`、`text(line)`、`clearText()`。

触发类型：`block_break`、`damage_dealt`、`damage_taken`、`health_recovered`、`movement`。
效果类型：`dynamic_modifier`、`hit_add_effect`、`absorb_damage`、`damage_burst`、`extend_mob_effects`、`place_light`、`projectile_accuracy`、`collect_drops`、`aoe_crop_growth`、`effectiveness`、`combined`。

已经绑定过的物品会保留原来的绑定 id，但读取新的数值；属性加成在物品重新装备时更新，烧瓶则会保留它绑定时的那瓶药水。

---

## 配置项

在服务器启动时与 `/reload` 时设置星辉魔法的配置值，不触碰配置文件。

```js
AstralSorcery.config(event => {
  event.set('general.dayLength', 30000)
  event.set('perks.perkLevelCap', 50)
  event.set('tiles.tree_beacon.range', 20)
  console.log(event.paths)   // 所有可用配置项
})
```

方法：`set(path, value)`、`get(path)`、`getDefault(path)`、`reset(path)`、`has(path)`、`paths`。

---

## 降星点

安放在降星点上的星能聚焦水晶石。

```js
AstralSorcery.focalPoint(event => {
  event.flightRadius(12)                      // 默认 8
  event.flightDuration(200)                   // 刻，默认 60
  event.starlightMultiplier(2)                // 产出的星能
  event.constellationMultiplier('vicio', 3)   // 只对某一个星座生效
  event.layerBonus(0.1)                       // 每层有效星辰晶柱 +10%
  event.linkDistance(24)                      // 默认 16
  event.transmutationSpeed(2)                 // 星能方块嬗变加快
  // event.disableFlight()
})
```
