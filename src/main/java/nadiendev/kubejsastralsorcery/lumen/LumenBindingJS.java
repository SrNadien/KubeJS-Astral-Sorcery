package nadiendev.kubejsastralsorcery.lumen;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.Context;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class LumenBindingJS {
    public static final List<String> SLOTS = List.of("helmet", "chestplate", "leggings", "boots", "melee_weapon", "ranged_weapon", "tool");

    private final ResourceLocation id;
    private JsonObject json;

    public LumenBindingJS(ResourceLocation id, JsonObject json) {
        this.id = id;
        this.json = json;

        if (!json.has("slot_bindings")) {
            json.add("slot_bindings", new JsonObject());
        }
    }

    public ResourceLocation getId() {
        return id;
    }

    public JsonObject getJson() {
        return json;
    }

    public LumenBindingJS raw(Context cx, Object replacement) {
        json = JsonUtils.of(cx, replacement).getAsJsonObject();
        return this;
    }

    private static JsonObject range(int min, int max) {
        JsonObject range = new JsonObject();
        range.addProperty("minInclusive", Math.min(min, max));
        range.addProperty("maxInclusive", Math.max(min, max));
        return range;
    }

    private JsonObject potionJson() {
        if (!json.has("potion_effect")) {
            throw new IllegalStateException("Binding " + id + " has no potion effect, set one with .potion(effect, min, max)");
        }

        return json.getAsJsonObject("potion_effect");
    }

    public LumenBindingJS potion(String effect, int minDuration, int maxDuration) {
        return potion(effect, minDuration, maxDuration, 0, 0);
    }

    public LumenBindingJS potion(String effect, int minDuration, int maxDuration, int minAmplifier, int maxAmplifier) {
        JsonObject potion = json.has("potion_effect") ? json.getAsJsonObject("potion_effect") : new JsonObject();
        potion.addProperty("effect", effect.indexOf(':') >= 0 ? effect : "minecraft:" + effect);
        potion.add("duration_range", range(minDuration, maxDuration));
        potion.add("amplifier_range", range(minAmplifier, maxAmplifier));
        json.add("potion_effect", potion);
        return this;
    }

    public LumenBindingJS potionDuration(int min, int max) {
        potionJson().add("duration_range", range(min, max));
        return this;
    }

    public LumenBindingJS potionAmplifier(int min, int max) {
        potionJson().add("amplifier_range", range(min, max));
        return this;
    }

    public LumenBindingJS hiddenPotion(String effect, int minDuration, int maxDuration, int minAmplifier, int maxAmplifier) {
        JsonObject hidden = new JsonObject();
        hidden.addProperty("effect", effect.indexOf(':') >= 0 ? effect : "minecraft:" + effect);
        hidden.add("duration_range", range(minDuration, maxDuration));
        hidden.add("amplifier_range", range(minAmplifier, maxAmplifier));
        potionJson().add("hidden_effect", hidden);
        return this;
    }

    public LumenBindingJS noPotion() {
        json.remove("potion_effect");
        return this;
    }

    private static String slotName(String slot) {
        String s = slot.toLowerCase(Locale.ROOT);

        if (!SLOTS.contains(s)) {
            throw new IllegalArgumentException("Unknown lumen binding slot '" + slot + "'. Valid: " + SLOTS);
        }

        return s;
    }

    public LumenSlotJS slot(String slot) {
        JsonObject slots = json.getAsJsonObject("slot_bindings");
        String name = slotName(slot);

        if (!slots.has(name)) {
            slots.add(name, new JsonObject());
        }

        return new LumenSlotJS(slots.getAsJsonObject(name));
    }

    public LumenBindingJS slot(String slot, Consumer<LumenSlotJS> modifier) {
        modifier.accept(slot(slot));
        return this;
    }

    public LumenBindingJS allSlots(Consumer<LumenSlotJS> modifier) {
        JsonObject slots = json.getAsJsonObject("slot_bindings");

        for (String name : SLOTS) {
            if (slots.has(name)) {
                modifier.accept(new LumenSlotJS(slots.getAsJsonObject(name)));
            }
        }

        return this;
    }

    public LumenBindingJS removeSlot(String slot) {
        json.getAsJsonObject("slot_bindings").remove(slotName(slot));
        return this;
    }

    public boolean hasSlot(String slot) {
        return json.getAsJsonObject("slot_bindings").has(slotName(slot));
    }

    public LumenBindingJS scaleModifiers(float factor) {
        return allSlots(s -> s.scaleModifiers(factor));
    }

    public LumenBindingJS cost(int lumenCost) {
        return allSlots(s -> s.cost(lumenCost));
    }

    public LumenBindingJS chance(float consumptionChance) {
        return allSlots(s -> s.chance(consumptionChance));
    }
}
