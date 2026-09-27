# Astral Sorcery KJS

**English** · [Español](wiky_es.md)

KubeJS support for **Astral Sorcery 2.0** (Minecraft 1.21.1, NeoForge). With this addon you can:

- create every Astral Sorcery recipe type from a script, with chainable builder methods;
- remove one recipe or many at once, with new Astral-specific filters;
- register **new constellations**;
- add **new altar tiers**, either as a new altar built on an existing tier or as a real fifth, sixth… tier;
- add, change and remove **tome (book) entries**;
- change the values of **lumen bindings**, **Astral Sorcery config** and the **focal point** effect.

Requirements: KubeJS `2101.7.2` or newer, Astral Sorcery `2.0.1` or newer.

Working scripts for every feature are in [`examples/`](examples/): copy `startup_scripts` and `server_scripts` into your `kubejs` folder.

> Astral Sorcery 2.0 is a full rewrite. It has no ritual pedestal: the per-constellation effects now live in
> **lumen bindings**, which is what the "rituals" section below edits.

## Table of contents

| What | Where | Script type |
|---|---|---|
| [Recipes](#recipes) | `AstralSorcery.recipes` | server |
| [Removing recipes](#removing-recipes) | `event.remove({...})` | server |
| [Constellations](#constellations) | `AstralSorcery.registry('constellations', ...)` | startup |
| [Altar tiers](#altar-tiers) | `StartupEvents.registry('block', ...)` + `AstralSorcery.altarTiers` | startup |
| [Tome entries](#tome-entries) | `AstralSorcery.research` | server |
| [Lumen bindings](#lumen-bindings-rituals) | `AstralSorcery.lumenBindings` | server |
| [Config values](#config-values) | `AstralSorcery.config` | server |
| [Focal point](#focal-point) | `AstralSorcery.focalPoint` | server |

Ids of constellations, lumen and altar effects can be written without a namespace: `'aevitas'` means
`'astralsorcery:aevitas'`. Colors accept `0x7F5FFF`, `'#7F5FFF'` or `'#FF7F5FFF'`.

---

## Recipes

Write Astral Sorcery recipes inside `AstralSorcery.recipes(event => { ... })`. The event has one function per
recipe type, plus the usual `remove`, `replaceInput`, `replaceOutput`, `forEachRecipe` and `findRecipes`.
The same types also work inside `ServerEvents.recipes` as `event.recipes.astralsorcery.<type>` or through the aliases:

| Recipe type | In `AstralSorcery.recipes` | In `ServerEvents.recipes` |
|---|---|---|
| Altar crafting | `event.altar_crafting` or `event.altar` | `event.recipes.astralAltar` |
| Focal combine | `event.focal_combine` or `event.combine` | `event.recipes.astralCombine` |
| Focal transmutation | `event.focal_transmutation` or `event.transmutation` | `event.recipes.astralTransmutation` |
| Lumen generation | `event.lumen_generation` or `event.lumen` | `event.recipes.astralLumen` |
| Lumen crystallization | `event.lumen_crystallization` or `event.crystallization` | `event.recipes.astralCrystallization` |
| Lightwell | `event.lightwell` | `event.recipes.astralLightwell` |
| Starlight infusion | `event.infusion` | `event.recipes.astralInfusion` |
| Liquid starlight | `event.liquid_starlight` | `event.recipes.astralLiquidStarlight` |
| Liquid interaction | `event.liquid_interaction` | `event.recipes.astralLiquidInteraction` |

The function only takes the values a recipe cannot work without. Everything else is set with a chained
method, and every recipe accepts `.id('pack:name')`.

### Altar crafting

```js
event.altar_crafting(output, pattern, key)
```

- `output`: an item or a list of items.
- `pattern`: up to 3 rows of up to 3 characters; spaces are empty slots. Short patterns are padded.
- `key`: a map from character to ingredient. A key can also be a fluid container requirement:
  `{ fluid: 'astralsorcery:liquid_starlight', amount: 1000 }`.

| Method | Meaning | Default |
|---|---|---|
| `.type(t)` | required altar tier: `illumination`, `resonance`, `luminance`, `radiance` or a [real tier](#real-tiers) | `illumination` |
| `.relay(pattern)` | 5×5 pattern of the focus relays around the altar; the centre must stay blank. Uses the same key | empty |
| `.define(char, ingredient)` | add or replace one key | |
| `.constellation(c)` | attuned focus crystal required in the altar (`.focus(c)` also works) | none |
| `.shatterChance(f)` | chance that the focus shatters | `0` |
| `.duration(ticks)` | crafting time (minimum 20) | `100` |
| `.anyTime()` / `.night()` / `.onlyNight(bool)` | only at night? | night only |
| `.chain()` / `.mayChain(bool)` | keep crafting while inputs remain | `false` |
| `.starlight(...constellations)` | starlight of these constellations must be beamed in | |
| `.lumen(lumen, amount)` | lumen consumed | |
| `.fluid(fluid, amount)` | fluid consumed (liquid in containers placed nearby) | |
| `.additional('4x minecraft:redstone')` | extra items consumed from nearby | |
| `.effect(id)` / `.noEffects()` | visual effects. Defaults follow the tier, like Astral Sorcery's own recipes | |
| `.altar('kubejs:my_altar')` | only this altar block can craft the recipe, see [altar tiers](#altar-tiers) | |

Output modifiers:

| Method | Effect |
|---|---|
| `.upgradesTo(block)` / `.setBlock(block)` | replaces the altar block instead of dropping an item (altar upgrades) |
| `.researchTier(tier)` | raises the crafting player's research tier |
| `.replaceWithInput(slot)` / `.replaceWithInput('relay', slot)` | output becomes a copy of that input |
| `.copyComponents('grid', slot)` / `.copyComponents('grid', slot, [components])` | copy data components from an input |
| `.setComponent(type, value)` | set a data component on the output |
| `.increaseEnchantments(chance)` | |
| `.gemModifier()`, `.enchantmentModifier()`, `.generateIdentifier()`, `.mergeCrystalProperties()` | |
| `.flag('is_artifact_enhanced')` | |
| `.artifactShardLoot(min, max)` | |
| `.crystalCount(perSize)` | |
| `.modifier({ type: '...', ... })` | any other modifier, as raw JSON |

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

### Focal combine

Items thrown into a focal point's beam are merged.

```js
event.focal_combine(outputs, inputs)
```

Methods: `.input(ingredient)`, `.output(item)`, `.duration(ticks)` (100), `.color(color)`, `.constellation(c)`.

### Focal transmutation

A focal point turns blocks under its beam into other blocks.

```js
event.focal_transmutation(outputBlocks, inputs)
```

- `outputBlocks`: a block id with its state inside the id (`'minecraft:cake[bites=2]'`), or a list of them; one is picked at random.
- `inputs`: block ids, `'#tag'`, or any vanilla block predicate JSON. The recipe matches if **any** input matches.

Methods: `.state(block)` / `.state(block, weight)` (extra output, weight 1 by default), `.input(predicate)`, `.display(ingredient)` (filled automatically from
plain block inputs), `.duration(ticks)` (100), `.color(color)`, `.constellation(c)`, `.focused(true)`
(needs starlight from a focus crystal beam).

### Lumen generation

```js
event.lumen_generation(lumen, catalyst)
```

Methods: `.amount(n)` (1), `.attemptMultiplier(f)` (1), `.starlightConsumption(f)` (1), `.shatterMultiplier(f)` (1),
`.combine(lumen, amount)`. A recipe with `.combine(...)` is made in the **Lumen Alchemy Array**, one without it in
the plain Lumen Array. Do not give two lumen generation recipes the same catalyst.

### Lumen crystallization

```js
event.lumen_crystallization(lumen, catalyst)
  .lumenPerOperation(900)   // 1-1900, default 1400
  .shatterMultiplier(3)     // default 1, 0 = never shatters
```

### Lightwell

```js
event.lightwell(fluid, catalyst)
  .productionMultiplier(1.2) // default 0.5
  .shatterMultiplier(0.2)    // default 10
  .color('#88CCFF')
```

### Starlight infusion

```js
event.infusion(output, input)
```

Methods: `.fluid(fluid)` (default `astralsorcery:liquid_starlight`), `.duration(ticks)` (200), `.consumptionChance(f)` (0.05), `.multipleFluids(bool)`, `.chalice(bool)` (true).

### Liquid starlight

Items thrown into a pool of liquid starlight.

```js
event.liquid_starlight(outputs, input)
```

`outputs` is an item (dropped) or a list of output modifiers; use `[]` together with one of the methods below.

Methods: `.with(ingredient)` (another item that must be in the pool), `.drop(item)`, `.duration(ticks)` (60),
`.randomDuration(ticks)` (20), `.color(c)`, `.consumesLiquid(bool)` (false), `.consumesInputs(bool)` (true),
`.mergeCrystal()`, `.formCrystalCluster()`, `.formGemCrystalCluster()`, `.growSize()`, `.bindLumen()`, `.fillLumen()`.

```js
event.liquid_starlight('minecraft:amethyst_shard', '2x minecraft:quartz')
  .with('minecraft:glowstone_dust')
  .consumesLiquid(true)
```

### Liquid interaction

Two fluids meeting in containment chalices.

```js
event.liquid_interaction(result, fluidA, fluidB)
  .chanceA(0.3).chanceB(1).weight(5)

event.liquid_interaction('minecraft:dirt', Fluid.of('minecraft:lava', 5), Fluid.of('minecraft:water', 5))
  .spawnEntity('minecraft:blaze')
```

### Replacing inputs and outputs

`event.replaceInput` and `event.replaceOutput` work on every Astral Sorcery recipe, including the altar grid keys,
relay slots, additional inputs and liquid starlight drops.

```js
event.replaceInput({ astral_type: 'altar' }, 'astralsorcery:stardust', 'minecraft:glowstone_dust')
```

---

## Removing recipes

All KubeJS filters work (`id`, `type`, `mod`, `input`, `output`, lists, `not`, `or`). The addon adds:

| Filter | Matches |
|---|---|
| `astral_type` | short type name: `altar`, `combine`, `transmutation`, `lumen`, `crystallization`, `lightwell`, `infusion`, `starlight`, `interaction` |
| `altar_tier` | altar recipes of that tier |
| `altar` | recipes locked to that altar block |
| `constellation` | any recipe using that constellation (focus, starlight, required constellation) |
| `focus` | altar recipes with that focus constellation |
| `lumen` | any recipe that makes or consumes that lumen |
| `fluid` | any recipe that uses that fluid |
| `output_block` | transmutations and altar upgrades that place that block |
| `research_tier` | altar recipes that raise the player to that research tier |

Every filter also accepts a list, meaning "any of them".

```js
event.remove({ id: 'astralsorcery:altar/illumination_wand' })                  // one recipe
event.remove([{ id: 'astralsorcery:infusion/glass_lens' }, { id: 'astralsorcery:infusion/resonating_gem' }])
event.remove({ altar_tier: 'radiance', output: 'astralsorcery:dynamism_gem_sky' })
event.remove({ astral_type: 'lightwell', fluid: 'minecraft:lava' })
event.remove({ lumen: ['caldor', 'hyle'], astral_type: 'lumen' })
event.remove({ constellation: 'discidia', astral_type: 'combine' })
```

---

## Constellations

In a **startup** script:

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

| Method | Meaning |
|---|---|
| `.color(c)` | constellation colour |
| `.major()` / `.minor()` / `.hidden()` | minor ones are only discoverable from the Resonance research tier; hidden ones never |
| `.star(x, y)`, `.stars([[x, y], ...])` | stars on a 0–31 grid, numbered in the order you add them |
| `.connect(a, b)`, `.connections([[a, b], ...])`, `.chain(a, b, c, ...)` | lines between stars, by star number |
| `.moonPhases('full', 'new', ...)` | fixed moon phases in which it shows in the sky. Without this it gets 5 random phases of 8 per world |
| `.seededMoonPhases(...)`, `.phaseDropOff(f)` | phases shifted by the world seed |
| `.focalPoint()` | focal points of this constellation generate in the world and can be found with the astrolabe |
| `.focusCrystal(layout)` | stellar filament layout for its focus crystal: `opposite_no_axis_symmetry`, `one_axis_symmetry`, `unique_distances`, `no_right_angles`, `no_dot_no_axis_symmetry`, `any` |
| `.focusSort('polygon' \| 'closest_first')` | how the filament beam is drawn |
| `.attunable()` | players can attune to it |
| `.rootPerk(type, x, y)` | creates its root perk in the perk tree. `type` picks how experience is earned: `aevitas` (placing blocks), `armara` (taking damage), `discidia` (dealing damage), `evorsio` (breaking blocks), `vicio` (moving). Needs a free position |
| `.rootPerkConnect(...perks)`, `.rootPerkModifier(attribute, mode, value)` | link the root into the tree and give it a bonus (`mode`: 0 add, 1 multiply base, 2 multiply total) |
| `.skyPosition(yaw, pitch)` | where it is drawn in the sky (yaw 0–359, pitch 10–80). One is added automatically if you give none |
| `.subtitle(text)`, `.description(text)` | tome texts |

Things to know:

- **The player attunement altar** finds constellations by placing focus relays at `(x/2 − 7, y/2 − 7)`. Avoid star
  layouts that collapse onto the same block or land on the altar (x and y of 14–15), and layouts that are a subset
  of an existing constellation.
- To give it a **lumen**, add a `focal_combine` or `focal_transmutation` recipe with `.constellation('kubejs:lyra')`
  that makes a flower, and a `lumen_generation` recipe for that flower.
- `StartupEvents.registry('astralsorcery:constellations', ...)` works too and takes the same methods.
- **Do not remove a constellation from a world that already used it.** Astral Sorcery cannot load player progress
  that refers to a missing constellation and resets it.

---

## Altar tiers

There are two ways to add an altar tier.

### New altar on an existing tier

A new altar block that uses the grid, GUI, sound and research tier of an existing tier, with its **own
multiblock structure** and, optionally, its **own exclusive recipes**.

```js
StartupEvents.registry('block', event => {
  event.create('kubejs:celestial_altar', 'astralsorcery:altar')
    .displayName('Celestial Altar')
    .baseTier('luminance')
    .exclusive()
    .structureBlock(0, -1, 0, 'minecraft:gold_block')
})
```

### Real tiers

A real new tier, above Radiance. Recipes can require it (`.type('stellar')`), it crafts every lower-tier recipe,
and it is shown in the tome and JEI with the grid of its base tier. Declare it in a startup script, **outside**
of any registry event, and then create its block:

```js
AstralSorcery.altarTiers(event => {
  event.create('stellar')
    .base('radiance')              // GUI, grid, shape and sound
    .researchTier('radiance')      // research tier given when the player gets the altar
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

### Altar block methods

| Method | Meaning |
|---|---|
| `.baseTier(t)` | `illumination`, `resonance`, `luminance` or `radiance`. Also picks the model |
| `.realTier(name)` | the block is an altar of that real tier |
| `.exclusive()` | the altar only crafts recipes locked to it |
| `.structureFrom(t)` | start from the structure of `none`, `resonance`, `resonance_expanded`, `luminance` or `radiance`. Default: the base tier |
| `.noBaseStructure()` | only the blocks you add |
| `.structureBlock(x, y, z, block)` | require a block relative to the altar |
| `.structureLayer(y, rows, key)` | require a layer of blocks; rows are centred on the altar, space = anything, `_` = air |

Blocks in structures can be a block id, a block state (`'minecraft:furnace[lit=true]'`), a tag (`'#minecraft:logs'`),
several ids (`'minecraft:stone|minecraft:andesite'`), `'air'`, or `'*'` to remove a requirement of the base structure.
All other KubeJS block methods (hardness, model, textures…) work too.

### Recipes for your altars

```js
event.altar_crafting('minecraft:nether_star', ['GGG', 'GDG', 'GGG'], { G: 'minecraft:gold_ingot', D: 'minecraft:diamond' })
  .type('luminance')
  .altar('kubejs:celestial_altar')   // only this block

event.altar_crafting('minecraft:elytra', ['FFF', 'FDF', 'FFF'], { F: 'minecraft:feather', D: 'minecraft:diamond' })
  .type('stellar')                   // real tier
```

To make players build it, add an altar recipe on the tier below with `.upgradesTo('kubejs:stellar_altar')`.
The wand shows the structure preview as usual, and the tome can show it with `.structure('kubejs:stellar_altar')`.

---

## Tome entries

In a **server** script. Entries are reloaded with `/reload` and sent to every player.

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

| Method | Meaning |
|---|---|
| `.title(text)` | name shown in the tome |
| `.tier(t)` | `shimmer`, `illumination`, `resonance`, `luminance`, `radiance`: the research cloud it belongs to, and when it becomes visible |
| `.position(x, y)` | position inside that cloud (existing entries use roughly x −8…5, y −9…4) |
| `.icon(items)`, `.lookup(items)` | icon, and items that link to this entry from recipe pages |
| `.connect(...ids)`, `.disconnect(...ids)` | lines to other entries of the same tier |
| `.text(text)` / `.texts(...)` | text page. Plain text or a lang key. `\\n` starts a paragraph; `<color="#RRGGBB">`, `<bold>`, `<italic>` work. About 20 lines fit a page |
| `.recipe(id)` / `.recipe(id, type)` / `.altarRecipe(id)` / `.craftingRecipe(id)` / `.infusionRecipe(id)` | recipe page |
| `.constellation(id)` | two-page spread for a constellation |
| `.structure(id, index)` | 3D structure page |
| `.lumen(lumen, ...slots)` | lumen binding page |
| `.emptyPage()`, `.insertText(index, text)`, `.removePage(index)`, `.clearPages()` | page editing |
| `.requiresConstellation(...ids)` | shown locked until one of them is discovered |
| `.requiresFlag('has_obtained_artifact')` | hidden until the flag is set |
| `.background(location, ...path)` | frame texture |

Recipe pages can show altar, crafting, infusion, lightwell, focal and lumen recipes.

---

## Lumen bindings (rituals)

In a **server** script. Changes apply on `/reload`.

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

  event.map('armara', 'kubejs:haste')   // armara lumen now binds this
  event.remove('discidia')
})
```

Event: `modify(id)`, `modify(id, fn)`, `modifyAll(fn)`, `create(id)`, `remove(...ids)`, `map(lumen, binding)`,
`getMapping(lumen)`, `ids`.

Binding: `potion(effect, minTicks, maxTicks, minAmp, maxAmp)`, `potionDuration(min, max)`, `potionAmplifier(min, max)`,
`hiddenPotion(...)`, `noPotion()`, `slot(name)`, `slot(name, fn)`, `allSlots(fn)`, `removeSlot(name)`,
`scaleModifiers(f)`, `cost(n)`, `chance(f)`, `raw(json)`.

Slots: `helmet`, `chestplate`, `leggings`, `boots`, `melee_weapon`, `ranged_weapon`, `tool`.

Slot: `cost(n)`, `chance(f)`, `usageType(type)`, `usage(json)`, `usageValue(field, value)`, `effect(json)`,
`addEffect(json)`, `effectValue(field, value)`, `modifier(attribute, mode, value)`, `modifierValue(attribute, value)`,
`removeModifier(attribute)`, `scaleModifiers(f)`, `text(line)`, `clearText()`.

Usage types: `block_break`, `damage_dealt`, `damage_taken`, `health_recovered`, `movement`.
Effect types: `dynamic_modifier`, `hit_add_effect`, `absorb_damage`, `damage_burst`, `extend_mob_effects`,
`place_light`, `projectile_accuracy`, `collect_drops`, `aoe_crop_growth`, `effectiveness`, `combined`.

Items already bound keep their binding id but read its new values; attribute bonuses update when the item is
equipped again, and a flask keeps the potion it was bound with.

---

## Config values

Sets Astral Sorcery config values when the server starts and on `/reload`, without touching the config file.

```js
AstralSorcery.config(event => {
  event.set('general.dayLength', 30000)
  event.set('perks.perkLevelCap', 50)
  event.set('tiles.tree_beacon.range', 20)
  console.log(event.paths)   // every available value
})
```

Methods: `set(path, value)`, `get(path)`, `getDefault(path)`, `reset(path)`, `has(path)`, `paths`.

---

## Focal point

The Starlight Focus Crystal set on a focal point.

```js
AstralSorcery.focalPoint(event => {
  event.flightRadius(12)                      // default 8
  event.flightDuration(200)                   // ticks, default 60
  event.starlightMultiplier(2)                // starlight produced
  event.constellationMultiplier('vicio', 3)   // only for one constellation
  event.layerBonus(0.1)                       // +10% per valid stellar filament layer
  event.linkDistance(24)                      // default 16
  event.transmutationSpeed(2)                 // focal transmutations run faster
  // event.disableFlight()
})
```
