package nadiendev.kubejsastralsorcery.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.ItemWrapper;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.rhino.Context;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ResearchNodeJS {
    public static final Set<String> TIERS = Set.of("shimmer", "illumination", "resonance", "luminance", "radiance");
    public static final Set<String> SLOTS = Set.of("helmet", "chestplate", "leggings", "boots", "melee_weapon", "ranged_weapon", "tool");

    private final ResourceLocation key;
    private final JsonObject json;
    private String title;

    public ResearchNodeJS(ResourceLocation key, JsonObject json) {
        this.key = key;
        this.json = json;
    }

    public static ResearchNodeJS create(ResourceLocation key) {
        JsonObject json = new JsonObject();
        json.addProperty("key", key.toString());
        json.addProperty("tier", "shimmer");
        json.addProperty("posX", 0F);
        json.addProperty("posY", 0F);
        JsonObject background = new JsonObject();
        background.addProperty("location", "screen");
        JsonArray path = new JsonArray();
        path.add("tome");
        path.add("research_frame_wood");
        background.add("path", path);
        json.add("backgroundTexture", background);
        json.add("renderItemStacks", new JsonArray());
        json.add("pages", new JsonArray());
        json.add("connections", new JsonArray());
        json.add("conditions", new JsonArray());
        json.add("lookupIndexItems", new JsonArray());
        return new ResearchNodeJS(key, json);
    }

    public ResourceLocation getKey() {
        return key;
    }

    public JsonObject getJson() {
        return json;
    }

    public String getTitle() {
        return title;
    }

    private JsonArray array(String name) {
        if (!json.has(name) || !json.get(name).isJsonArray()) {
            json.add(name, new JsonArray());
        }

        return json.getAsJsonArray(name);
    }

    private static String asId(String raw) {
        return (raw.indexOf(':') >= 0 ? ResourceLocation.parse(raw) : AstralSorceryKJS.as(raw)).toString();
    }

    public ResearchNodeJS title(String title) {
        this.title = title;
        return this;
    }

    public ResearchNodeJS tier(String tier) {
        String t = tier.toLowerCase(Locale.ROOT);

        if (!TIERS.contains(t)) {
            throw new IllegalArgumentException("Unknown research tier '" + tier + "'. Valid: " + TIERS);
        }

        json.addProperty("tier", t);
        return this;
    }

    public ResearchNodeJS position(float x, float y) {
        json.addProperty("posX", x);
        json.addProperty("posY", y);
        return this;
    }

    public ResearchNodeJS pos(float x, float y) {
        return position(x, y);
    }

    public ResearchNodeJS icon(Context cx, Object items) {
        JsonArray icons = new JsonArray();

        for (Object o : ListJS.orSelf(items)) {
            ItemStack stack = ItemWrapper.wrap(cx, o);

            if (!stack.isEmpty()) {
                icons.add(ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack).getOrThrow());
            }
        }

        json.add("renderItemStacks", icons);
        return this;
    }

    public ResearchNodeJS lookup(Context cx, Object items) {
        JsonArray lookup = array("lookupIndexItems");

        for (Object o : ListJS.orSelf(items)) {
            ItemStack stack = ItemWrapper.wrap(cx, o);

            if (!stack.isEmpty()) {
                lookup.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            }
        }

        return this;
    }

    public ResearchNodeJS clearLookup() {
        json.add("lookupIndexItems", new JsonArray());
        return this;
    }

    public ResearchNodeJS connect(String... nodes) {
        JsonArray connections = array("connections");

        for (String node : nodes) {
            String id = asId(node);

            if (!contains(connections, id)) {
                connections.add(id);
            }
        }

        return this;
    }

    public ResearchNodeJS disconnect(String... nodes) {
        JsonArray connections = array("connections");

        for (String node : nodes) {
            String id = asId(node);

            for (int i = connections.size() - 1; i >= 0; i--) {
                if (connections.get(i).getAsString().equals(id)) {
                    connections.remove(i);
                }
            }
        }

        return this;
    }

    public ResearchNodeJS clearConnections() {
        json.add("connections", new JsonArray());
        return this;
    }

    public ResearchNodeJS background(String location, String... path) {
        JsonObject background = new JsonObject();
        background.addProperty("location", location);
        JsonArray array = new JsonArray();

        for (String p : path) {
            array.add(p);
        }

        background.add("path", array);
        json.add("backgroundTexture", background);
        return this;
    }

    private ResearchNodeJS page(JsonObject page) {
        array("pages").add(page);
        return this;
    }

    private static JsonObject pageOf(String type) {
        JsonObject page = new JsonObject();
        page.addProperty("type", "astralsorcery:" + type);
        return page;
    }

    public ResearchNodeJS text(String text) {
        JsonObject page = pageOf("text");
        page.addProperty("textKey", text);
        return page(page);
    }

    public ResearchNodeJS texts(String... texts) {
        for (String text : texts) {
            text(text);
        }

        return this;
    }

    public ResearchNodeJS emptyPage() {
        return page(pageOf("empty"));
    }

    public ResearchNodeJS recipe(String recipeId) {
        JsonObject page = pageOf("recipe");
        page.addProperty("recipeId", recipeId.indexOf(':') >= 0 ? recipeId : "minecraft:" + recipeId);
        page.addProperty("recipe_type", ResearchInjector.UNRESOLVED_TYPE);
        return page(page);
    }

    public ResearchNodeJS recipe(String recipeId, String recipeType) {
        JsonObject page = pageOf("recipe");
        page.addProperty("recipeId", recipeId.indexOf(':') >= 0 ? recipeId : "minecraft:" + recipeId);
        page.addProperty("recipe_type", recipeType.indexOf(':') >= 0 ? recipeType : AstralSorceryKJS.as(recipeType).toString());
        return page(page);
    }

    public ResearchNodeJS altarRecipe(String recipeId) {
        return recipe(recipeId, "astralsorcery:altar_crafting");
    }

    public ResearchNodeJS craftingRecipe(String recipeId) {
        return recipe(recipeId, "minecraft:crafting");
    }

    public ResearchNodeJS infusionRecipe(String recipeId) {
        return recipe(recipeId, "astralsorcery:infusion");
    }

    public ResearchNodeJS structure(String observer) {
        return structure(observer, 0);
    }

    public ResearchNodeJS structure(String observer, int index) {
        JsonObject page = pageOf("structure");
        page.addProperty("structureObserver", asId(observer));
        page.addProperty("structureIndex", index);
        return page(page);
    }

    public ResearchNodeJS constellation(String constellation) {
        if (array("pages").size() % 2 != 0) {
            emptyPage();
        }

        String id = asId(constellation);

        for (int i = 0; i < 2; i++) {
            JsonObject page = pageOf("constellation");
            page.addProperty("structureObserver", id);
            page(page);
        }

        return this;
    }

    public ResearchNodeJS lumen(String lumen, String... slots) {
        JsonObject page = pageOf("lumen_description");
        page.addProperty("lumen", asId(lumen));
        JsonArray array = new JsonArray();
        List<String> list = slots.length == 0 ? List.of("helmet", "chestplate", "leggings", "boots", "melee_weapon", "ranged_weapon", "tool") : List.of(slots);

        for (String slot : list) {
            String s = slot.toLowerCase(Locale.ROOT);

            if (!SLOTS.contains(s)) {
                throw new IllegalArgumentException("Unknown lumen binding slot '" + slot + "'. Valid: " + SLOTS);
            }

            array.add(s);
        }

        page.add("slots", array);
        return page(page);
    }

    public ResearchNodeJS insertText(int index, String text) {
        JsonObject page = pageOf("text");
        page.addProperty("textKey", text);
        JsonArray pages = array("pages");
        JsonArray rebuilt = new JsonArray();

        for (int i = 0; i < pages.size(); i++) {
            if (i == index) {
                rebuilt.add(page);
            }

            rebuilt.add(pages.get(i));
        }

        if (index >= pages.size()) {
            rebuilt.add(page);
        }

        json.add("pages", rebuilt);
        return this;
    }

    public ResearchNodeJS removePage(int index) {
        JsonArray pages = array("pages");

        if (index >= 0 && index < pages.size()) {
            pages.remove(index);
        }

        return this;
    }

    public ResearchNodeJS clearPages() {
        json.add("pages", new JsonArray());
        return this;
    }

    public int getPageCount() {
        return array("pages").size();
    }

    public ResearchNodeJS requiresConstellation(String... constellations) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "astralsorcery:constellation_discovered");
        JsonArray list = new JsonArray();

        for (String c : constellations) {
            list.add(asId(c));
        }

        condition.add("any_constellations", list);
        array("conditions").add(condition);
        return this;
    }

    public ResearchNodeJS requiresFlag(String flag) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "astralsorcery:research_flag_set");
        condition.addProperty("flag", flag.toLowerCase(Locale.ROOT));
        array("conditions").add(condition);
        return this;
    }

    public ResearchNodeJS clearConditions() {
        json.add("conditions", new JsonArray());
        return this;
    }

    private static boolean contains(JsonArray array, String value) {
        for (JsonElement e : array) {
            if (e.isJsonPrimitive() && e.getAsString().equals(value)) {
                return true;
            }
        }

        return false;
    }
}
