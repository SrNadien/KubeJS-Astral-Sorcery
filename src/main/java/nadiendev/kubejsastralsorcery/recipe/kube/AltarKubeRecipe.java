package nadiendev.kubejsastralsorcery.recipe.kube;

import nadiendev.kubejsastralsorcery.recipe.AstralFormat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.fluid.FluidWrapper;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.BlockWrapper;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.kubejs.util.TinyMap;
import dev.latvian.mods.rhino.Context;
import hellfirepvp.astralsorcery.common.ingredient.IngredientBridge;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.lumen.Lumen;
import hellfirepvp.astralsorcery.common.lumen.LumenStack;
import hellfirepvp.astralsorcery.common.recipe.altar.effect.AltarEffect;
import hellfirepvp.astralsorcery.common.recipe.altar.output.AltarRecipeOutputModifier;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import nadiendev.kubejsastralsorcery.recipe.component.AstralComponents;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static nadiendev.kubejsastralsorcery.recipe.schema.AstralSchemas.*;

public class AltarKubeRecipe extends AstralKubeRecipe {
    private boolean noEffects = false;

    public KubeRecipe define(Context cx, String character, Object ingredient) {
        if (character.length() != 1 || Character.isWhitespace(character.charAt(0))) {
            throw new IllegalArgumentException("Grid keys must be a single non-blank character, got '" + character + "'");
        }

        IngredientBridge bridge = wrapWith(cx, AstralComponents.of(AstralComponents.INGREDIENT_BRIDGE), ingredient);
        TinyMap<Character, IngredientBridge> current = getValue(ALTAR_KEY);
        List<TinyMap.Entry<Character, IngredientBridge>> entries = new ArrayList<>();

        if (current != null) {
            for (TinyMap.Entry<Character, IngredientBridge> entry : current.entries()) {
                if (entry.key() != character.charAt(0)) {
                    entries.add(entry);
                }
            }
        }

        entries.add(new TinyMap.Entry<>(character.charAt(0), bridge));
        return setValue(ALTAR_KEY, new TinyMap<>(entries));
    }

    public KubeRecipe type(Context cx, Object tier) {
        return setValue(ALTAR_TYPE, wrapWith(cx, ALTAR_TYPE.component, tier));
    }

    public KubeRecipe starlight(Context cx, Object... constellations) {
        for (Object constellation : constellations) {
            addWrapped(cx, ALTAR_STARLIGHT, constellation);
        }

        return this;
    }

    public KubeRecipe lumen(Context cx, Object lumen, int amount) {
        Lumen value = wrapWith(cx, LUMEN, lumen);
        return addTo(ALTAR_LUMEN, LumenStack.of(value, amount));
    }

    public KubeRecipe fluid(Context cx, Object fluid) {
        return addTo(ALTAR_FLUIDS, (FluidStack) cx.jsToJava(fluid, FluidWrapper.TYPE_INFO));
    }

    public KubeRecipe fluid(Context cx, Object fluid, int amount) {
        FluidStack stack = (FluidStack) cx.jsToJava(fluid, FluidWrapper.TYPE_INFO);
        return addTo(ALTAR_FLUIDS, stack.copyWithAmount(amount));
    }

    public KubeRecipe additional(Context cx, Object ingredient) {
        return addWrapped(cx, ALTAR_ADDITIONAL, ingredient);
    }

    public KubeRecipe additionalInput(Context cx, Object ingredient) {
        return additional(cx, ingredient);
    }

    public KubeRecipe effect(Context cx, Object effect) {
        return addWrapped(cx, ALTAR_EFFECTS, effect);
    }

    public KubeRecipe noEffects() {
        noEffects = true;
        return setValue(ALTAR_EFFECTS, List.of());
    }

    public KubeRecipe modifier(Context cx, Object modifier) {
        return addWrapped(cx, ALTAR_MODIFIERS, modifier);
    }

    private KubeRecipe modifier(Context cx, JsonObject json) {
        return addTo(ALTAR_MODIFIERS, parse(cx, AltarRecipeOutputModifier.CODEC, json));
    }

    public KubeRecipe upgradesTo(Context cx, Object block) {
        BlockState state = block instanceof BlockState s ? s : BlockWrapper.parseBlockState(scx(cx).registries(), String.valueOf(block));
        JsonObject entry = new JsonObject();
        entry.add("data", BlockState.CODEC.encodeStart(scx(cx).ops().json(), state).getOrThrow());
        entry.addProperty("weight", 1);
        JsonArray states = new JsonArray();
        states.add(entry);
        JsonObject json = typed("set_block");
        json.add("block_states", states);
        return modifier(cx, json);
    }

    public KubeRecipe setBlock(Context cx, Object block) {
        return upgradesTo(cx, block);
    }

    public KubeRecipe researchTier(Context cx, String tier) {
        JsonObject json = typed("update_research_tier");
        json.addProperty("tier", tier.toLowerCase(Locale.ROOT));
        return modifier(cx, json);
    }

    public KubeRecipe replaceWithInput(Context cx, String slotType, int slot) {
        JsonObject json = typed("replace_with_input");
        json.addProperty("slot_type", slotType(slotType));
        json.addProperty("input_slot", slot);
        return modifier(cx, json);
    }

    public KubeRecipe replaceWithInput(Context cx, int slot) {
        return replaceWithInput(cx, "altar_grid", slot);
    }

    public KubeRecipe copyComponents(Context cx, String slotType, int slot) {
        JsonObject json = typed("copy_data_components");
        json.addProperty("slot_type", slotType(slotType));
        json.addProperty("input_slot", slot);
        json.addProperty("copy_all", true);
        json.add("components_to_copy", new JsonArray());
        return modifier(cx, json);
    }

    public KubeRecipe copyComponents(Context cx, String slotType, int slot, String[] components) {
        JsonObject json = typed("copy_data_components");
        json.addProperty("slot_type", slotType(slotType));
        json.addProperty("input_slot", slot);
        json.addProperty("copy_all", false);
        JsonArray list = new JsonArray();
        Arrays.stream(components).forEach(list::add);
        json.add("components_to_copy", list);
        return modifier(cx, json);
    }

    public KubeRecipe setComponent(Context cx, String componentType, Object value) {
        JsonObject json = typed("set_data_component");
        json.addProperty(AstralFormat.key("componentType"), componentType);
        JsonElement element = JsonUtils.of(cx, value);
        json.add("value", element);
        return modifier(cx, json);
    }

    public KubeRecipe increaseEnchantments(Context cx, float additionalLevelChance) {
        JsonObject json = typed("increase_enchantments");
        json.addProperty("additional_level_chance", additionalLevelChance);
        return modifier(cx, json);
    }

    public KubeRecipe gemModifier(Context cx) {
        return modifier(cx, typed("add_gem_modifier"));
    }

    public KubeRecipe enchantmentModifier(Context cx) {
        return modifier(cx, typed("add_enchantment_modifier"));
    }

    public KubeRecipe generateIdentifier(Context cx) {
        return modifier(cx, typed("generate_identifier"));
    }

    public KubeRecipe mergeCrystalProperties(Context cx) {
        return modifier(cx, typed("merge_crystal_properties"));
    }

    public KubeRecipe flag(Context cx, String flag) {
        JsonObject json = typed("set_flag");
        json.addProperty("flag", flag.toLowerCase(Locale.ROOT));
        return modifier(cx, json);
    }

    public KubeRecipe artifactShardLoot(Context cx, int min, int max) {
        JsonObject range = new JsonObject();
        range.addProperty(AstralFormat.rangeKey("minInclusive"), min);
        range.addProperty(AstralFormat.rangeKey("maxInclusive"), max);
        JsonObject json = typed("generate_artifact_shard_loot");
        json.add("count_range", range);
        return modifier(cx, json);
    }

    public KubeRecipe crystalCount(Context cx, int countPerSize) {
        JsonObject json = typed("set_crystal_count");
        json.addProperty("count_per_size", countPerSize);
        return modifier(cx, json);
    }

    public KubeRecipe night() {
        return setValue(ALTAR_ONLY_NIGHT, true);
    }

    public KubeRecipe anyTime() {
        return setValue(ALTAR_ONLY_NIGHT, false);
    }

    public KubeRecipe chain() {
        return setValue(ALTAR_MAY_CHAIN, true);
    }

    private static String slotType(String raw) {
        String s = raw.toLowerCase(Locale.ROOT);
        return switch (s) {
            case "grid", "altar", "altar_grid" -> "altar_grid";
            case "relay", "relay_item" -> "relay_item";
            default -> throw new IllegalArgumentException("Unknown slot type '" + raw + "', use 'altar_grid' or 'relay_item'");
        };
    }

    @Override
    public void serialize() {
        setValue(ALTAR_PATTERN, normalizePattern(getValue(ALTAR_PATTERN), 3));
        List<String> relay = normalizePattern(getValue(ALTAR_RELAY), 5);

        if (!Character.isWhitespace(relay.get(2).charAt(2))) {
            throw new IllegalArgumentException("The center of the relay pattern is the altar itself and must stay empty");
        }

        setValue(ALTAR_RELAY, relay);
        List<AltarEffect> effects = getValue(ALTAR_EFFECTS);

        if (!noEffects && (effects == null || effects.isEmpty())) {
            setValue(ALTAR_EFFECTS, defaultEffects(getValue(ALTAR_TYPE)));
        }

        super.serialize();
    }

    public static List<AltarEffect> defaultEffects(TileAltar.AltarType type) {
        List<AltarEffect> effects = new ArrayList<>();
        effects.add(RegistriesAS.REGISTRY_ALTAR_EFFECTS.get(AstralSorceryKJS.as("default_central_beam")));

        if (type != null && type.isThisLater(TileAltar.AltarType.ILLUMINATION)) {
            effects.add(RegistriesAS.REGISTRY_ALTAR_EFFECTS.get(AstralSorceryKJS.as("default_altar_sparkle")));
        }

        effects.add(RegistriesAS.REGISTRY_ALTAR_EFFECTS.get(AstralSorceryKJS.as("default_lumen_input")));
        effects.add(RegistriesAS.REGISTRY_ALTAR_EFFECTS.get(AstralSorceryKJS.as("default_relay_input")));
        effects.removeIf(e -> e == null);
        return effects;
    }
}
