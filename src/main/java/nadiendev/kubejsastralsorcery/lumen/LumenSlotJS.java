package nadiendev.kubejsastralsorcery.lumen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class LumenSlotJS {
    private final JsonObject json;

    public LumenSlotJS(JsonObject json) {
        this.json = json;

        if (!json.has("display_text")) {
            json.add("display_text", new JsonArray());
        }

        if (!json.has("effect")) {
            json.add("effect", typed("none"));
        }

        if (!json.has("lumen_usage")) {
            json.add("lumen_usage", typed("none"));
        }
    }

    public JsonObject getJson() {
        return json;
    }

    static JsonObject typed(String type) {
        JsonObject o = new JsonObject();
        o.addProperty("type", type.indexOf(':') >= 0 ? type : "astralsorcery:" + type);
        return o;
    }

    private static String type(JsonObject o) {
        return o.has("type") ? o.get("type").getAsString() : "";
    }

    public LumenSlotJS effect(Context cx, Object effect) {
        json.add("effect", JsonUtils.of(cx, effect));
        return this;
    }

    public LumenSlotJS addEffect(Context cx, Object effect) {
        JsonObject current = json.getAsJsonObject("effect");
        JsonElement added = JsonUtils.of(cx, effect);

        if (type(current).equals("astralsorcery:none")) {
            json.add("effect", added);
        } else if (type(current).equals("astralsorcery:combined")) {
            current.getAsJsonArray("effects").add(added);
        } else {
            JsonObject combined = typed("combined");
            JsonArray effects = new JsonArray();
            effects.add(current);
            effects.add(added);
            combined.add("effects", effects);
            json.add("effect", combined);
        }

        return this;
    }

    public LumenSlotJS usage(Context cx, Object usage) {
        json.add("lumen_usage", JsonUtils.of(cx, usage));
        return this;
    }

    public LumenSlotJS usageType(String type) {
        JsonObject usage = typed(type);
        JsonObject old = json.getAsJsonObject("lumen_usage");
        usage.addProperty("lumen_cost", old.has("lumen_cost") ? old.get("lumen_cost").getAsInt() : 1);
        usage.addProperty("consumption_chance", old.has("consumption_chance") ? old.get("consumption_chance").getAsFloat() : 0.2F);

        if (usage.get("type").getAsString().equals("astralsorcery:damage_dealt")) {
            usage.addProperty("direct", true);
        }

        if (usage.get("type").getAsString().equals("astralsorcery:movement")) {
            JsonArray stats = new JsonArray();
            stats.add("minecraft:walk_one_cm");
            stats.add("minecraft:sprint_one_cm");
            usage.add("stat_id", stats);
            usage.addProperty("stat_multiplier", 0.02F);
        }

        json.add("lumen_usage", usage);
        return this;
    }

    private void forEachUsage(Consumer<JsonObject> consumer) {
        forEach(json.getAsJsonObject("lumen_usage"), "usages", consumer);
    }

    private void forEachEffect(Consumer<JsonObject> consumer) {
        forEach(json.getAsJsonObject("effect"), "effects", consumer);
    }

    private static void forEach(JsonObject root, String childList, Consumer<JsonObject> consumer) {
        if (root == null) {
            return;
        }

        consumer.accept(root);

        if (root.has(childList) && root.get(childList).isJsonArray()) {
            for (JsonElement e : root.getAsJsonArray(childList)) {
                if (e.isJsonObject()) {
                    forEach(e.getAsJsonObject(), childList, consumer);
                }
            }
        }
    }

    public LumenSlotJS cost(int lumenCost) {
        forEachUsage(u -> {
            if (u.has("lumen_cost")) {
                u.addProperty("lumen_cost", lumenCost);
            }
        });
        return this;
    }

    public LumenSlotJS chance(float consumptionChance) {
        forEachUsage(u -> {
            if (u.has("consumption_chance")) {
                u.addProperty("consumption_chance", consumptionChance);
            }
        });
        return this;
    }

    public LumenSlotJS usageValue(String field, Object value) {
        forEachUsage(u -> {
            if (u.has(field)) {
                set(u, field, value);
            }
        });
        return this;
    }

    public LumenSlotJS effectValue(String field, Object value) {
        forEachEffect(e -> {
            if (e.has(field)) {
                set(e, field, value);
            }
        });
        return this;
    }

    private static void set(JsonObject o, String field, Object value) {
        if (value instanceof Number n) {
            o.addProperty(field, n);
        } else if (value instanceof Boolean b) {
            o.addProperty(field, b);
        } else {
            o.addProperty(field, String.valueOf(value));
        }
    }

    private List<JsonObject> modifiers() {
        List<JsonObject> list = new ArrayList<>();
        forEachEffect(e -> {
            if (type(e).equals("astralsorcery:dynamic_modifier") && e.has("modifiers")) {
                e.getAsJsonArray("modifiers").forEach(m -> list.add(m.getAsJsonObject()));
            }
        });
        return list;
    }

    public LumenSlotJS scaleModifiers(float factor) {
        for (JsonObject m : modifiers()) {
            m.addProperty("value", m.get("value").getAsFloat() * factor);
        }

        return this;
    }

    public LumenSlotJS modifierValue(String attribute, float value) {
        String id = attribute.indexOf(':') >= 0 ? attribute : "astralsorcery:" + attribute;

        for (JsonObject m : modifiers()) {
            if (m.get("attribute_type").getAsString().equals(id)) {
                m.addProperty("value", value);
            }
        }

        return this;
    }

    public LumenSlotJS modifier(String attribute, int mode, float value) {
        JsonObject modifier = new JsonObject();
        modifier.addProperty("attribute_type", attribute.indexOf(':') >= 0 ? attribute : "astralsorcery:" + attribute);
        modifier.addProperty("identifier", UUID.nameUUIDFromBytes((json.toString() + attribute + mode).getBytes()).toString());
        modifier.addProperty("mode", mode);
        modifier.addProperty("value", value);

        JsonObject[] target = new JsonObject[1];
        forEachEffect(e -> {
            if (target[0] == null && type(e).equals("astralsorcery:dynamic_modifier")) {
                target[0] = e;
            }
        });

        if (target[0] != null) {
            target[0].getAsJsonArray("modifiers").add(modifier);
            return this;
        }

        JsonObject dynamic = typed("dynamic_modifier");
        JsonArray list = new JsonArray();
        list.add(modifier);
        dynamic.add("modifiers", list);
        JsonObject current = json.getAsJsonObject("effect");

        if (type(current).equals("astralsorcery:none")) {
            json.add("effect", dynamic);
        } else if (type(current).equals("astralsorcery:combined")) {
            current.getAsJsonArray("effects").add(dynamic);
        } else {
            JsonObject combined = typed("combined");
            JsonArray effects = new JsonArray();
            effects.add(current);
            effects.add(dynamic);
            combined.add("effects", effects);
            json.add("effect", combined);
        }

        return this;
    }

    public LumenSlotJS removeModifier(String attribute) {
        String id = attribute.indexOf(':') >= 0 ? attribute : "astralsorcery:" + attribute;
        forEachEffect(e -> {
            if (type(e).equals("astralsorcery:dynamic_modifier") && e.has("modifiers")) {
                JsonArray mods = e.getAsJsonArray("modifiers");

                for (int i = mods.size() - 1; i >= 0; i--) {
                    if (mods.get(i).getAsJsonObject().get("attribute_type").getAsString().equals(id)) {
                        mods.remove(i);
                    }
                }
            }
        });
        return this;
    }

    public LumenSlotJS text(String text) {
        JsonObject component = new JsonObject();
        component.addProperty("text", text);
        json.getAsJsonArray("display_text").add(component);
        return this;
    }

    public LumenSlotJS clearText() {
        json.add("display_text", new JsonArray());
        return this;
    }
}
