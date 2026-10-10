# Changelog

## 2101.1.1 — Minecraft 1.21.1 / NeoForge

- Compatible with the newer Astral Sorcery builds, which replaced `CountIngredient` with NeoForge's `SizedIngredient`. It keeps working with 2.0.1.20, the version on CurseForge.
- Recipes, lumen binding ranges and altar modifiers use the key style of the installed Astral Sorcery (camelCase or snake_case) and accept both when reading.

## 2101.1.0 — Minecraft 1.21.1 / NeoForge

- **Recipes:** every Astral Sorcery recipe type (altar, focal combine, focal transmutation, lumen generation, lumen crystallization, lightwell, infusion, liquid starlight and liquid interaction) from a script, with chainable methods, plus new filters to remove them.
- **Constellations:** register new constellations with stars, connections, moon phases, focal points, focus crystal layouts and root perks.
- **Altar tiers:** new altars with their own multiblock structure and exclusive recipes, and real new tiers above Radiance.
- **Tome:** add, change and remove research entries and their pages.
- **Lumen bindings, config and focal point:** change them from a script.
- Fixes a crash of Astral Sorcery when Curios is not installed.
