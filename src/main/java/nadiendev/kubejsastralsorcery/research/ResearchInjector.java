package nadiendev.kubejsastralsorcery.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.script.ScriptType;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import nadiendev.kubejsastralsorcery.AstralSorceryEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public final class ResearchInjector {
    public static final String UNRESOLVED_TYPE = AstralSorceryKJS.MOD_ID + ":auto";
    private static final Map<ResourceLocation, String> TITLES = new HashMap<>();
    private static volatile Map<ResourceLocation, String> clientTitles = Map.of();
    private static RecipeManager latestRecipes;

    private ResearchInjector() {
    }

    public static void setLatestRecipes(RecipeManager manager) {
        latestRecipes = manager;
    }

    public static Map<ResourceLocation, String> serverTitles() {
        synchronized (TITLES) {
            return Map.copyOf(TITLES);
        }
    }

    public static void setClientTitles(Map<ResourceLocation, String> titles) {
        clientTitles = Map.copyOf(titles);
    }

    public static Optional<String> title(ResourceLocation key) {
        String t = clientTitles.get(key);

        if (t == null) {
            synchronized (TITLES) {
                t = TITLES.get(key);
            }
        }

        return Optional.ofNullable(t);
    }

    public static void inject(Map<ResourceLocation, JsonElement> dataMap) {
        synchronized (TITLES) {
            TITLES.clear();
        }

        if (!AstralSorceryEvents.RESEARCH.hasListeners()) {
            return;
        }

        Map<ResourceLocation, JsonObject> byKey = new HashMap<>();
        Map<ResourceLocation, ResourceLocation> fileByKey = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : dataMap.entrySet()) {
            if (entry.getValue().isJsonObject() && entry.getValue().getAsJsonObject().has("key")) {
                ResourceLocation key = ResourceLocation.tryParse(entry.getValue().getAsJsonObject().get("key").getAsString());

                if (key != null) {
                    byKey.put(key, entry.getValue().getAsJsonObject());
                    fileByKey.put(key, entry.getKey());
                }
            }
        }

        ResearchKubeEvent event = new ResearchKubeEvent(byKey);

        try {
            AstralSorceryEvents.RESEARCH.post(ScriptType.SERVER, event);
        } catch (Exception ex) {
            AstralSorceryKJS.LOGGER.error("AstralSorceryEvents.research failed", ex);
            return;
        }

        for (ResourceLocation key : event.removed) {
            ResourceLocation file = fileByKey.get(key);

            if (file != null) {
                dataMap.remove(file);
            }
        }

        Iterator<Map.Entry<ResourceLocation, JsonElement>> it = dataMap.entrySet().iterator();

        while (it.hasNext()) {
            JsonElement value = it.next().getValue();

            if (value.isJsonObject() && value.getAsJsonObject().has("connections")) {
                JsonArray connections = value.getAsJsonObject().getAsJsonArray("connections");

                for (int i = connections.size() - 1; i >= 0; i--) {
                    ResourceLocation target = ResourceLocation.tryParse(connections.get(i).getAsString());

                    if (target != null && event.removed.contains(target)) {
                        connections.remove(i);
                    }
                }
            }
        }

        for (ResearchNodeJS node : event.modified.values()) {
            finish(node);
            ResourceLocation file = fileByKey.getOrDefault(node.getKey(), node.getKey());
            dataMap.put(file, node.getJson());
        }

        for (ResearchNodeJS node : event.created.values()) {
            finish(node);
            ResourceLocation file = fileByKey.containsKey(node.getKey()) ? fileByKey.get(node.getKey()) : node.getKey().withPrefix(AstralSorceryKJS.MOD_ID + "_generated/");
            dataMap.put(file, node.getJson());
        }

        AstralSorceryKJS.LOGGER.info("Research event: {} created, {} modified, {} removed", event.created.size(), event.modified.size(), event.removed.size());
    }

    private static void finish(ResearchNodeJS node) {
        JsonObject json = node.getJson();

        if (node.getTitle() != null) {
            synchronized (TITLES) {
                TITLES.put(node.getKey(), node.getTitle());
            }
        }

        if (json.getAsJsonArray("renderItemStacks").isEmpty()) {
            JsonArray lookup = json.getAsJsonArray("lookupIndexItems");
            JsonObject icon = new JsonObject();
            icon.addProperty("id", lookup != null && !lookup.isEmpty() ? lookup.get(0).getAsString() : "astralsorcery:tome");
            icon.addProperty("count", 1);
            json.getAsJsonArray("renderItemStacks").add(icon);
        }

        for (JsonElement page : json.getAsJsonArray("pages")) {
            if (page.isJsonObject() && UNRESOLVED_TYPE.equals(stringOf(page.getAsJsonObject(), "recipe_type"))) {
                ResourceLocation recipeId = ResourceLocation.parse(page.getAsJsonObject().get("recipeId").getAsString());
                page.getAsJsonObject().addProperty("recipe_type", resolveType(recipeId));
            }
        }
    }

    private static String stringOf(JsonObject json, String key) {
        return json.has(key) ? json.get(key).getAsString() : null;
    }

    private static String resolveType(ResourceLocation recipeId) {
        if (latestRecipes != null) {
            Optional<RecipeHolder<?>> holder = latestRecipes.byKey(recipeId);

            if (holder.isPresent()) {
                ResourceLocation type = BuiltInRegistries.RECIPE_TYPE.getKey(holder.get().value().getType());

                if (type != null) {
                    return type.toString();
                }
            }
        }

        String path = recipeId.getPath();

        for (String prefix : new String[]{"altar", "infusion", "lightwell", "focal_combine", "focal_transmutation", "lumen_crystallization"}) {
            if (path.startsWith(prefix + "/")) {
                return prefix.equals("altar") ? "astralsorcery:altar_crafting" : "astralsorcery:" + prefix;
            }
        }

        if (path.startsWith("lumen/")) {
            return "astralsorcery:lumen_generation";
        }

        AstralSorceryKJS.LOGGER.warn("Could not resolve the recipe type of '{}' for a tome page, assuming minecraft:crafting. Use .recipe(id, type) to set it", recipeId);
        return "minecraft:crafting";
    }
}
