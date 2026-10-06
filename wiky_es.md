# Astral Sorcery KJS

[English](wiky.md) · **Español** · [简体中文](wiky_zh_cn.md)

Soporte de KubeJS para **Astral Sorcery 2.0** (Minecraft 1.21.1, NeoForge). Con este addon puedes:

- crear cualquier tipo de receta de Astral Sorcery desde un script, con métodos encadenados;
- eliminar una receta o varias a la vez, con filtros nuevos propios de Astral;
- registrar **constelaciones nuevas**;
- añadir **tiers de altar nuevos**, ya sea como un altar nuevo sobre un tier existente o como un quinto, sexto… tier real;
- añadir, cambiar y eliminar **entradas del tomo** (el libro);
- cambiar los valores de los **lumen bindings**, de la **config de Astral Sorcery** y del efecto del **focal point**.

Requisitos: KubeJS `2101.7.2` o superior y Astral Sorcery `2.0.1` o superior.

En [`examples/`](examples/) hay scripts funcionando para todo lo que sigue: copia `startup_scripts` y `server_scripts` a tu carpeta `kubejs`.

> Astral Sorcery 2.0 es una reescritura completa. Ya no tiene el pedestal de rituales: los efectos de cada
> constelación viven ahora en los **lumen bindings**, que es lo que edita la sección de "rituales".

## Índice

| Qué | Dónde | Tipo de script |
|---|---|---|
| [Recetas](#recetas) | `AstralSorcery.recipes` | server |
| [Eliminar recetas](#eliminar-recetas) | `event.remove({...})` | server |
| [Constelaciones](#constelaciones) | `AstralSorcery.registry('constellations', ...)` | startup |
| [Tiers de altar](#tiers-de-altar) | `StartupEvents.registry('block', ...)` + `AstralSorcery.altarTiers` | startup |
| [Entradas del tomo](#entradas-del-tomo) | `AstralSorcery.research` | server |
| [Lumen bindings](#lumen-bindings-rituales) | `AstralSorcery.lumenBindings` | server |
| [Config](#config) | `AstralSorcery.config` | server |
| [Focal point](#focal-point) | `AstralSorcery.focalPoint` | server |

Los ids de constelaciones, lumen y efectos del altar se pueden escribir sin namespace: `'aevitas'` equivale a
`'astralsorcery:aevitas'`. Los colores aceptan `0x7F5FFF`, `'#7F5FFF'` o `'#FF7F5FFF'`.

---

## Recetas

Las recetas de Astral Sorcery se escriben dentro de `AstralSorcery.recipes(event => { ... })`. El evento tiene una
función por cada tipo de receta, además de los habituales `remove`, `replaceInput`, `replaceOutput`, `forEachRecipe`
y `findRecipes`. Los mismos tipos funcionan también dentro de `ServerEvents.recipes`, como
`event.recipes.astralsorcery.<tipo>` o con los alias:

| Tipo de receta | En `AstralSorcery.recipes` | En `ServerEvents.recipes` |
|---|---|---|
| Altar | `event.altar_crafting` o `event.altar` | `event.recipes.astralAltar` |
| Combinación focal | `event.focal_combine` o `event.combine` | `event.recipes.astralCombine` |
| Transmutación focal | `event.focal_transmutation` o `event.transmutation` | `event.recipes.astralTransmutation` |
| Generación de lumen | `event.lumen_generation` o `event.lumen` | `event.recipes.astralLumen` |
| Cristalización de lumen | `event.lumen_crystallization` o `event.crystallization` | `event.recipes.astralCrystallization` |
| Lightwell | `event.lightwell` | `event.recipes.astralLightwell` |
| Infusión de starlight | `event.infusion` | `event.recipes.astralInfusion` |
| Liquid starlight | `event.liquid_starlight` | `event.recipes.astralLiquidStarlight` |
| Interacción de líquidos | `event.liquid_interaction` | `event.recipes.astralLiquidInteraction` |

La función solo recibe lo que la receta necesita sí o sí. Todo lo demás se pone con un método encadenado, y toda
receta acepta `.id('pack:nombre')`.

### Altar

```js
event.altar_crafting(salida, patron, clave)
```

- `salida`: un ítem o una lista de ítems.
- `patron`: hasta 3 filas de hasta 3 caracteres; los espacios son huecos vacíos. Los patrones cortos se rellenan.
- `clave`: un mapa de carácter a ingrediente. Una clave también puede pedir un contenedor con fluido:
  `{ fluid: 'astralsorcery:liquid_starlight', amount: 1000 }`.

| Método | Qué hace | Por defecto |
|---|---|---|
| `.type(t)` | tier de altar necesario: `illumination`, `resonance`, `luminance`, `radiance` o un [tier real](#tiers-reales) | `illumination` |
| `.relay(patron)` | patrón 5×5 de los focus relays alrededor del altar; el centro debe quedar vacío. Usa la misma clave | vacío |
| `.define(caracter, ingrediente)` | añade o reemplaza una clave | |
| `.constellation(c)` | cristal de foco sintonizado que pide el altar (también vale `.focus(c)`) | ninguno |
| `.shatterChance(f)` | probabilidad de que el foco se rompa | `0` |
| `.duration(ticks)` | tiempo de crafteo (mínimo 20) | `100` |
| `.anyTime()` / `.night()` / `.onlyNight(bool)` | ¿solo de noche? | solo de noche |
| `.chain()` / `.mayChain(bool)` | sigue crafteando mientras queden ingredientes | `false` |
| `.starlight(...constelaciones)` | hay que dirigir al altar starlight de esas constelaciones | |
| `.lumen(lumen, cantidad)` | lumen que consume | |
| `.fluid(fluido, cantidad)` | fluido que consume (de contenedores puestos cerca) | |
| `.additional('4x minecraft:redstone')` | ítems extra que consume de alrededor | |
| `.effect(id)` / `.noEffects()` | efectos visuales. Por defecto dependen del tier, igual que en las recetas de Astral Sorcery | |
| `.altar('kubejs:mi_altar')` | solo ese bloque de altar puede craftearla, ver [tiers de altar](#tiers-de-altar) | |

Modificadores de la salida:

| Método | Efecto |
|---|---|
| `.upgradesTo(bloque)` / `.setBlock(bloque)` | reemplaza el bloque del altar en vez de soltar un ítem (mejoras de altar) |
| `.researchTier(tier)` | sube el tier de investigación del jugador que craftea |
| `.replaceWithInput(slot)` / `.replaceWithInput('relay', slot)` | la salida pasa a ser una copia de ese ingrediente |
| `.copyComponents('grid', slot)` / `.copyComponents('grid', slot, [componentes])` | copia data components de un ingrediente |
| `.setComponent(tipo, valor)` | pone un data component en la salida |
| `.increaseEnchantments(probabilidad)` | |
| `.gemModifier()`, `.enchantmentModifier()`, `.generateIdentifier()`, `.mergeCrystalProperties()` | |
| `.flag('is_artifact_enhanced')` | |
| `.artifactShardLoot(min, max)` | |
| `.crystalCount(porTamaño)` | |
| `.modifier({ type: '...', ... })` | cualquier otro modificador, en JSON |

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

### Combinación focal

Los ítems que se tiran en el rayo de un focal point se fusionan.

```js
event.focal_combine(salidas, ingredientes)
```

Métodos: `.input(ingrediente)`, `.output(item)`, `.duration(ticks)` (100), `.color(color)`, `.constellation(c)`.

### Transmutación focal

Un focal point convierte los bloques bajo su rayo en otros bloques.

```js
event.focal_transmutation(bloquesSalida, entradas)
```

- `bloquesSalida`: un id de bloque con el estado dentro del id (`'minecraft:cake[bites=2]'`), o una lista; se elige uno al azar.
- `entradas`: ids de bloque, `'#tag'` o cualquier JSON de block predicate de vanilla. La receta vale si **cualquiera** coincide.

Métodos: `.state(bloque)` / `.state(bloque, peso)` (otra salida, peso 1 por defecto), `.input(predicado)`,
`.display(ingrediente)` (se rellena solo con las entradas de bloque simples), `.duration(ticks)` (100), `.color(color)`,
`.constellation(c)`, `.focused(true)` (necesita starlight del rayo de un cristal de foco).

### Generación de lumen

```js
event.lumen_generation(lumen, catalizador)
```

Métodos: `.amount(n)` (1), `.attemptMultiplier(f)` (1), `.starlightConsumption(f)` (1), `.shatterMultiplier(f)` (1),
`.combine(lumen, cantidad)`. Una receta con `.combine(...)` se hace en el **Lumen Alchemy Array**; sin él, en el Lumen
Array normal. No pongas el mismo catalizador en dos recetas de generación de lumen.

### Cristalización de lumen

```js
event.lumen_crystallization(lumen, catalizador)
  .lumenPerOperation(900)   // 1-1900, por defecto 1400
  .shatterMultiplier(3)     // por defecto 1; 0 = nunca se rompe
```

### Lightwell

```js
event.lightwell(fluido, catalizador)
  .productionMultiplier(1.2) // por defecto 0.5
  .shatterMultiplier(0.2)    // por defecto 10
  .color('#88CCFF')
```

### Infusión de starlight

```js
event.infusion(salida, entrada)
```

Métodos: `.fluid(fluido)` (por defecto `astralsorcery:liquid_starlight`), `.duration(ticks)` (200),
`.consumptionChance(f)` (0.05), `.multipleFluids(bool)`, `.chalice(bool)` (true).

### Liquid starlight

Ítems que se tiran a un charco de liquid starlight.

```js
event.liquid_starlight(salidas, entrada)
```

`salidas` es un ítem (se suelta) o una lista de modificadores de salida; usa `[]` junto con alguno de los métodos de abajo.

Métodos: `.with(ingrediente)` (otro ítem que tiene que estar en el charco), `.drop(item)`, `.duration(ticks)` (60),
`.randomDuration(ticks)` (20), `.color(c)`, `.consumesLiquid(bool)` (false), `.consumesInputs(bool)` (true),
`.mergeCrystal()`, `.formCrystalCluster()`, `.formGemCrystalCluster()`, `.growSize()`, `.bindLumen()`, `.fillLumen()`.

```js
event.liquid_starlight('minecraft:amethyst_shard', '2x minecraft:quartz')
  .with('minecraft:glowstone_dust')
  .consumesLiquid(true)
```

### Interacción de líquidos

Dos fluidos que se juntan en containment chalices.

```js
event.liquid_interaction(resultado, fluidoA, fluidoB)
  .chanceA(0.3).chanceB(1).weight(5)

event.liquid_interaction('minecraft:dirt', Fluid.of('minecraft:lava', 5), Fluid.of('minecraft:water', 5))
  .spawnEntity('minecraft:blaze')
```

### Reemplazar ingredientes y salidas

`event.replaceInput` y `event.replaceOutput` funcionan en todas las recetas de Astral Sorcery, incluidas las claves del
grid del altar, los relays, los ingredientes adicionales y lo que suelta el liquid starlight.

```js
event.replaceInput({ astral_type: 'altar' }, 'astralsorcery:stardust', 'minecraft:glowstone_dust')
```

---

## Eliminar recetas

Funcionan todos los filtros de KubeJS (`id`, `type`, `mod`, `input`, `output`, listas, `not`, `or`). El addon añade:

| Filtro | Coincide con |
|---|---|
| `astral_type` | nombre corto del tipo: `altar`, `combine`, `transmutation`, `lumen`, `crystallization`, `lightwell`, `infusion`, `starlight`, `interaction` |
| `altar_tier` | recetas de altar de ese tier |
| `altar` | recetas exclusivas de ese bloque de altar |
| `constellation` | cualquier receta que use esa constelación (foco, starlight, constelación requerida) |
| `focus` | recetas de altar con esa constelación de foco |
| `lumen` | cualquier receta que produzca o consuma ese lumen |
| `fluid` | cualquier receta que use ese fluido |
| `output_block` | transmutaciones y mejoras de altar que colocan ese bloque |
| `research_tier` | recetas de altar que suben al jugador a ese tier de investigación |

Todos los filtros aceptan también una lista, que significa "cualquiera de estos".

```js
event.remove({ id: 'astralsorcery:altar/illumination_wand' })                  // una receta
event.remove([{ id: 'astralsorcery:infusion/glass_lens' }, { id: 'astralsorcery:infusion/resonating_gem' }])
event.remove({ altar_tier: 'radiance', output: 'astralsorcery:dynamism_gem_sky' })
event.remove({ astral_type: 'lightwell', fluid: 'minecraft:lava' })
event.remove({ lumen: ['caldor', 'hyle'], astral_type: 'lumen' })
event.remove({ constellation: 'discidia', astral_type: 'combine' })
```

---

## Constelaciones

En un script de **startup**:

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

| Método | Qué hace |
|---|---|
| `.color(c)` | color de la constelación |
| `.major()` / `.minor()` / `.hidden()` | las menores solo se descubren desde el tier de investigación Resonance; las ocultas nunca |
| `.star(x, y)`, `.stars([[x, y], ...])` | estrellas en una cuadrícula de 0 a 31, numeradas en el orden en que las añades |
| `.connect(a, b)`, `.connections([[a, b], ...])`, `.chain(a, b, c, ...)` | líneas entre estrellas, por número de estrella |
| `.moonPhases('full', 'new', ...)` | fases lunares fijas en las que sale en el cielo. Sin esto, cada mundo le asigna 5 de las 8 fases al azar |
| `.seededMoonPhases(...)`, `.phaseDropOff(f)` | fases desplazadas según la semilla del mundo |
| `.focalPoint()` | se generan focal points de esta constelación en el mundo y se encuentran con el astrolabio |
| `.focusCrystal(disposición)` | disposición de stellar filaments para su cristal de foco: `opposite_no_axis_symmetry`, `one_axis_symmetry`, `unique_distances`, `no_right_angles`, `no_dot_no_axis_symmetry`, `any` |
| `.focusSort('polygon' \| 'closest_first')` | cómo se dibuja el rayo de los filaments |
| `.attunable()` | los jugadores pueden sintonizarse con ella |
| `.rootPerk(tipo, x, y)` | crea su perk raíz en el árbol de perks. `tipo` decide cómo se gana experiencia: `aevitas` (colocando bloques), `armara` (recibiendo daño), `discidia` (haciendo daño), `evorsio` (rompiendo bloques), `vicio` (moviéndose). Necesita una posición libre |
| `.rootPerkConnect(...perks)`, `.rootPerkModifier(atributo, modo, valor)` | enlaza la raíz al árbol y le da una bonificación (`modo`: 0 suma, 1 multiplica la base, 2 multiplica el total) |
| `.skyPosition(yaw, pitch)` | dónde se dibuja en el cielo (yaw 0–359, pitch 10–80). Si no pones ninguna se añade una sola |
| `.subtitle(texto)`, `.description(texto)` | textos del tomo |

A tener en cuenta:

- **El altar de sintonización del jugador** reconoce las constelaciones poniendo focus relays en `(x/2 − 7, y/2 − 7)`.
  Evita disposiciones en las que dos estrellas caigan en el mismo bloque o encima del altar (x e y de 14–15), y las que
  sean un subconjunto de otra constelación.
- Para darle un **lumen**, añade una receta `focal_combine` o `focal_transmutation` con `.constellation('kubejs:lyra')`
  que haga una flor, y una receta `lumen_generation` para esa flor.
- `StartupEvents.registry('astralsorcery:constellations', ...)` también funciona, con los mismos métodos.
- **No quites una constelación de un mundo que ya la usó.** Astral Sorcery no puede cargar el progreso de un jugador
  que menciona una constelación que no existe y lo reinicia.

---

## Tiers de altar

Hay dos formas de añadir un tier de altar.

### Altar nuevo sobre un tier existente

Un bloque de altar nuevo que usa la cuadrícula, la GUI, el sonido y el tier de investigación de un tier que ya existe,
con **su propia estructura multibloque** y, si quieres, **sus propias recetas exclusivas**.

```js
StartupEvents.registry('block', event => {
  event.create('kubejs:celestial_altar', 'astralsorcery:altar')
    .displayName('Celestial Altar')
    .baseTier('luminance')
    .exclusive()
    .structureBlock(0, -1, 0, 'minecraft:gold_block')
})
```

### Tiers reales

Un tier nuevo de verdad, por encima de Radiance. Las recetas pueden pedirlo (`.type('stellar')`), craftea todas las
recetas de tiers inferiores y en el tomo y en JEI se muestra con la cuadrícula de su tier base. Se declara en un script
de startup, **fuera** de cualquier evento de registro, y luego se crea su bloque:

```js
AstralSorcery.altarTiers(event => {
  event.create('stellar')
    .base('radiance')              // GUI, cuadrícula, forma y sonido
    .researchTier('radiance')      // tier de investigación que da al conseguir el altar
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

### Métodos del bloque de altar

| Método | Qué hace |
|---|---|
| `.baseTier(t)` | `illumination`, `resonance`, `luminance` o `radiance`. También elige el modelo |
| `.realTier(nombre)` | el bloque es un altar de ese tier real |
| `.exclusive()` | el altar solo craftea las recetas exclusivas suyas |
| `.structureFrom(t)` | parte de la estructura de `none`, `resonance`, `resonance_expanded`, `luminance` o `radiance`. Por defecto la del tier base |
| `.noBaseStructure()` | solo los bloques que añadas tú |
| `.structureBlock(x, y, z, bloque)` | pide un bloque en una posición relativa al altar |
| `.structureLayer(y, filas, clave)` | pide una capa de bloques; las filas se centran en el altar, espacio = cualquier cosa, `_` = aire |

En las estructuras un bloque puede ser un id, un estado (`'minecraft:furnace[lit=true]'`), un tag (`'#minecraft:logs'`),
varios ids (`'minecraft:stone|minecraft:andesite'`), `'air'`, o `'*'` para quitar un requisito de la estructura base.
Todos los demás métodos de bloque de KubeJS (dureza, modelo, texturas…) también funcionan.

### Recetas para tus altares

```js
event.altar_crafting('minecraft:nether_star', ['GGG', 'GDG', 'GGG'], { G: 'minecraft:gold_ingot', D: 'minecraft:diamond' })
  .type('luminance')
  .altar('kubejs:celestial_altar')   // solo este bloque

event.altar_crafting('minecraft:elytra', ['FFF', 'FDF', 'FFF'], { F: 'minecraft:feather', D: 'minecraft:diamond' })
  .type('stellar')                   // tier real
```

Para que los jugadores lo construyan, añade una receta de altar en el tier de abajo con `.upgradesTo('kubejs:stellar_altar')`.
La varita enseña la vista previa de la estructura como siempre, y el tomo puede mostrarla con `.structure('kubejs:stellar_altar')`.

---

## Entradas del tomo

En un script de **server**. Las entradas se recargan con `/reload` y se envían a todos los jugadores.

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

| Método | Qué hace |
|---|---|
| `.title(texto)` | nombre que se ve en el tomo |
| `.tier(t)` | `shimmer`, `illumination`, `resonance`, `luminance`, `radiance`: la nube de investigación en la que va, y desde cuándo se ve |
| `.position(x, y)` | posición dentro de esa nube (las entradas existentes usan más o menos x −8…5, y −9…4) |
| `.icon(items)`, `.lookup(items)` | icono, e ítems que enlazan a esta entrada desde las páginas de recetas |
| `.connect(...ids)`, `.disconnect(...ids)` | líneas hacia otras entradas del mismo tier |
| `.text(texto)` / `.texts(...)` | página de texto. Texto normal o una clave de lang. `\\n` empieza un párrafo; funcionan `<color="#RRGGBB">`, `<bold>` y `<italic>`. Caben unas 20 líneas por página |
| `.recipe(id)` / `.recipe(id, tipo)` / `.altarRecipe(id)` / `.craftingRecipe(id)` / `.infusionRecipe(id)` | página de receta |
| `.constellation(id)` | doble página de una constelación |
| `.structure(id, indice)` | página con la estructura en 3D |
| `.lumen(lumen, ...slots)` | página de lumen binding |
| `.emptyPage()`, `.insertText(indice, texto)`, `.removePage(indice)`, `.clearPages()` | editar páginas |
| `.requiresConstellation(...ids)` | se ve bloqueada hasta descubrir alguna de ellas |
| `.requiresFlag('has_obtained_artifact')` | oculta hasta que se activa ese flag |
| `.background(ubicacion, ...ruta)` | textura del marco |

Las páginas de receta pueden mostrar recetas de altar, crafteo, infusión, lightwell, focales y de lumen.

---

## Lumen bindings (rituales)

En un script de **server**. Los cambios se aplican con `/reload`.

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

  event.map('armara', 'kubejs:haste')   // el lumen armara ahora aplica este binding
  event.remove('discidia')
})
```

Evento: `modify(id)`, `modify(id, fn)`, `modifyAll(fn)`, `create(id)`, `remove(...ids)`, `map(lumen, binding)`,
`getMapping(lumen)`, `ids`.

Binding: `potion(efecto, minTicks, maxTicks, minAmp, maxAmp)`, `potionDuration(min, max)`, `potionAmplifier(min, max)`,
`hiddenPotion(...)`, `noPotion()`, `slot(nombre)`, `slot(nombre, fn)`, `allSlots(fn)`, `removeSlot(nombre)`,
`scaleModifiers(f)`, `cost(n)`, `chance(f)`, `raw(json)`.

Slots: `helmet`, `chestplate`, `leggings`, `boots`, `melee_weapon`, `ranged_weapon`, `tool`.

Slot: `cost(n)`, `chance(f)`, `usageType(tipo)`, `usage(json)`, `usageValue(campo, valor)`, `effect(json)`,
`addEffect(json)`, `effectValue(campo, valor)`, `modifier(atributo, modo, valor)`, `modifierValue(atributo, valor)`,
`removeModifier(atributo)`, `scaleModifiers(f)`, `text(linea)`, `clearText()`.

Tipos de uso: `block_break`, `damage_dealt`, `damage_taken`, `health_recovered`, `movement`.
Tipos de efecto: `dynamic_modifier`, `hit_add_effect`, `absorb_damage`, `damage_burst`, `extend_mob_effects`,
`place_light`, `projectile_accuracy`, `collect_drops`, `aoe_crop_growth`, `effectiveness`, `combined`.

Los ítems ya vinculados conservan el id del binding pero leen sus valores nuevos; las bonificaciones de atributos se
actualizan al volver a equipar el ítem, y un frasco conserva la poción con la que se vinculó.

---

## Config

Pone valores de la config de Astral Sorcery al arrancar el servidor y con `/reload`, sin tocar el fichero de config.

```js
AstralSorcery.config(event => {
  event.set('general.dayLength', 30000)
  event.set('perks.perkLevelCap', 50)
  event.set('tiles.tree_beacon.range', 20)
  console.log(event.paths)   // todos los valores disponibles
})
```

Métodos: `set(ruta, valor)`, `get(ruta)`, `getDefault(ruta)`, `reset(ruta)`, `has(ruta)`, `paths`.

---

## Focal point

El Starlight Focus Crystal colocado sobre un focal point.

```js
AstralSorcery.focalPoint(event => {
  event.flightRadius(12)                      // por defecto 8
  event.flightDuration(200)                   // ticks, por defecto 60
  event.starlightMultiplier(2)                // starlight que produce
  event.constellationMultiplier('vicio', 3)   // solo para una constelación
  event.layerBonus(0.1)                       // +10% por cada capa válida de stellar filaments
  event.linkDistance(24)                      // por defecto 16
  event.transmutationSpeed(2)                 // las transmutaciones focales van más rápido
  // event.disableFlight()
})
```
