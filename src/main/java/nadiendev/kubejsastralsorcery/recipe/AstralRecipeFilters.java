package nadiendev.kubejsastralsorcery.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.filter.RecipeFilter;
import dev.latvian.mods.kubejs.recipe.filter.RecipeFilterParseEvent;
import dev.latvian.mods.kubejs.recipe.filter.TypeFilter;
import dev.latvian.mods.kubejs.recipe.filter.OrFilter;
import dev.latvian.mods.kubejs.util.ListJS;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public final class AstralRecipeFilters {
    public static final Map<String, String> SHORT_TYPES = Map.ofEntries(
        Map.entry("altar", "altar_crafting"),
        Map.entry("altar_crafting", "altar_crafting"),
        Map.entry("combine", "focal_combine"),
        Map.entry("focal_combine", "focal_combine"),
        Map.entry("transmutation", "focal_transmutation"),
        Map.entry("focal_transmutation", "focal_transmutation"),
        Map.entry("lumen", "lumen_generation"),
        Map.entry("lumen_generation", "lumen_generation"),
        Map.entry("crystallization", "lumen_crystallization"),
        Map.entry("lumen_crystallization", "lumen_crystallization"),
        Map.entry("lightwell", "lightwell"),
        Map.entry("infusion", "infusion"),
        Map.entry("liquid_starlight", "liquid_starlight"),
        Map.entry("starlight", "liquid_starlight"),
        Map.entry("liquid_interaction", "liquid_interaction"),
        Map.entry("interaction", "liquid_interaction")
    );

    private AstralRecipeFilters() {
    }

    @SubscribeEvent
    public static void onParse(RecipeFilterParseEvent event) {
        Map<?, ?> map = event.map;

        add(event, map.get("astral_type"), value -> {
            String path = SHORT_TYPES.get(value.toLowerCase(Locale.ROOT));

            if (path == null) {
                throw new IllegalArgumentException("Unknown Astral Sorcery recipe type '" + value + "'. Valid: " + SHORT_TYPES.keySet());
            }

            return new TypeFilter(AstralSorceryKJS.as(path));
        });

        add(event, map.get("altar_tier"), value -> {
            String tier = value.toLowerCase(Locale.ROOT);
            return json(json -> json.has("requiredType") && tier.equals(json.get("requiredType").getAsString()));
        });

        add(event, map.get("altar"), value -> {
            String block = id(value, "minecraft").toString();
            String field = AstralSorceryKJS.MOD_ID + ":altar";
            return json(json -> json.has(field) && block.equals(json.get(field).getAsString()));
        });

        add(event, map.get("focus"), value -> {
            String id = id(value, AstralSorceryKJS.AS_ID).toString();
            return json(json -> json.has("focusConstellation") && id.equals(json.get("focusConstellation").getAsString()));
        });

        add(event, map.get("constellation"), value -> {
            String id = id(value, AstralSorceryKJS.AS_ID).toString();
            return json(json -> collect(json, AstralRecipeFilters::constellations).contains(id));
        });

        add(event, map.get("lumen"), value -> {
            String id = id(value, AstralSorceryKJS.AS_ID).toString();
            return json(json -> collect(json, AstralRecipeFilters::lumens).contains(id));
        });

        add(event, map.get("fluid"), value -> {
            String id = id(value, "minecraft").toString();
            return json(json -> collect(json, AstralRecipeFilters::fluids).contains(id));
        });

        add(event, map.get("output_block"), value -> {
            String id = id(value, "minecraft").toString();
            return json(json -> collect(json, AstralRecipeFilters::outputBlocks).contains(id));
        });

        add(event, map.get("research_tier"), value -> {
            String tier = value.toLowerCase(Locale.ROOT);
            return json(json -> {
                if (!json.has("outputModifiers") || !json.get("outputModifiers").isJsonArray()) {
                    return false;
                }

                for (JsonElement e : json.getAsJsonArray("outputModifiers")) {
                    if (e.isJsonObject() && "astralsorcery:update_research_tier".equals(string(e.getAsJsonObject(), "type")) && tier.equals(string(e.getAsJsonObject(), "tier"))) {
                        return true;
                    }
                }

                return false;
            });
        });
    }

    private interface JsonPredicate {
        boolean test(JsonObject json);
    }

    private interface Factory {
        RecipeFilter create(String value);
    }

    private static RecipeFilter json(JsonPredicate predicate) {
        return cx -> cx.recipe() instanceof KubeRecipe recipe && recipe.json != null && predicate.test(recipe.json);
    }

    private static void add(RecipeFilterParseEvent event, Object raw, Factory factory) {
        if (raw == null) {
            return;
        }

        List<RecipeFilter> filters = new ArrayList<>();

        for (Object o : ListJS.orSelf(raw)) {
            filters.add(factory.create(String.valueOf(o).trim()));
        }

        if (filters.size() == 1) {
            event.filters.add(filters.getFirst());
        } else if (!filters.isEmpty()) {
            OrFilter or = new OrFilter();
            or.list.addAll(filters);
            event.filters.add(or);
        }
    }

    private static ResourceLocation id(String value, String namespace) {
        return value.indexOf(':') >= 0 ? ResourceLocation.parse(value) : ResourceLocation.fromNamespaceAndPath(namespace, value);
    }

    private static Set<String> collect(JsonObject json, BiConsumer<JsonObject, Set<String>> collector) {
        Set<String> set = new HashSet<>();
        collector.accept(json, set);
        return set;
    }

    private static String string(JsonObject json, String key) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsString() : null;
    }

    private static void addString(JsonObject json, String key, Set<String> into) {
        String s = string(json, key);

        if (s != null) {
            into.add(s);
        }
    }

    private static void constellations(JsonObject json, Set<String> into) {
        addString(json, "focusConstellation", into);
        addString(json, "required_constellation", into);

        if (json.has("requiredStarlight") && json.get("requiredStarlight").isJsonArray()) {
            json.getAsJsonArray("requiredStarlight").forEach(e -> into.add(e.getAsString()));
        }
    }

    private static void lumens(JsonObject json, Set<String> into) {
        addString(json, "produced_lumen", into);
        addString(json, "lumen_to_crystallize", into);

        if (json.has("requiredLumen") && json.get("requiredLumen").isJsonArray()) {
            json.getAsJsonArray("requiredLumen").forEach(e -> {
                if (e.isJsonObject()) {
                    addString(e.getAsJsonObject(), "lumen", into);
                }
            });
        }

        if (json.has("lumen_combination_inputs") && json.get("lumen_combination_inputs").isJsonObject()) {
            into.addAll(json.getAsJsonObject("lumen_combination_inputs").keySet());
        }
    }

    private static void fluids(JsonObject json, Set<String> into) {
        addString(json, "fluid_input", into);
        addString(json, "generated_fluid", into);

        if (json.has("requiredFluid") && json.get("requiredFluid").isJsonArray()) {
            json.getAsJsonArray("requiredFluid").forEach(e -> {
                if (e.isJsonObject()) {
                    addString(e.getAsJsonObject(), "id", into);
                }
            });
        }

        for (String reactant : List.of("reactantA", "reactantB")) {
            if (json.has(reactant) && json.get(reactant).isJsonObject()) {
                sizedFluid(json.getAsJsonObject(reactant), into);
            }
        }

        if (json.has("grid") && json.get("grid").isJsonObject()) {
            JsonObject grid = json.getAsJsonObject("grid");

            if (grid.has("key") && grid.get("key").isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : grid.getAsJsonObject("key").entrySet()) {
                    if (entry.getValue().isJsonObject() && "fluid".equals(string(entry.getValue().getAsJsonObject(), "type"))) {
                        JsonElement ingredient = entry.getValue().getAsJsonObject().get("ingredient");

                        if (ingredient != null && ingredient.isJsonObject()) {
                            sizedFluid(ingredient.getAsJsonObject(), into);
                        }
                    }
                }
            }
        }
    }

    private static void sizedFluid(JsonObject sized, Set<String> into) {
        JsonElement ingredient = sized.has("ingredient") ? sized.get("ingredient") : sized;
        JsonArray array = new JsonArray();

        if (ingredient.isJsonArray()) {
            array = ingredient.getAsJsonArray();
        } else {
            array.add(ingredient);
        }

        for (JsonElement e : array) {
            if (e.isJsonObject()) {
                addString(e.getAsJsonObject(), "fluid", into);
            }
        }
    }

    private static void outputBlocks(JsonObject json, Set<String> into) {
        if (json.has("output_states") && json.get("output_states").isJsonArray()) {
            json.getAsJsonArray("output_states").forEach(e -> {
                if (e.isJsonObject() && e.getAsJsonObject().has("data") && e.getAsJsonObject().get("data").isJsonObject()) {
                    addString(e.getAsJsonObject().getAsJsonObject("data"), "Name", into);
                }
            });
        }

        if (json.has("outputModifiers") && json.get("outputModifiers").isJsonArray()) {
            json.getAsJsonArray("outputModifiers").forEach(e -> {
                if (e.isJsonObject() && "astralsorcery:set_block".equals(string(e.getAsJsonObject(), "type")) && e.getAsJsonObject().has("block_states")) {
                    e.getAsJsonObject().getAsJsonArray("block_states").forEach(s -> {
                        if (s.isJsonObject() && s.getAsJsonObject().has("data")) {
                            addString(s.getAsJsonObject().getAsJsonObject("data"), "Name", into);
                        }
                    });
                }
            });
        }
    }
}
