package nadiendev.kubejsastralsorcery.research;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.KubeEvent;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ResearchKubeEvent implements KubeEvent {
    private final Map<ResourceLocation, JsonObject> existing;
    final Map<ResourceLocation, ResearchNodeJS> created = new LinkedHashMap<>();
    final Map<ResourceLocation, ResearchNodeJS> modified = new LinkedHashMap<>();
    final Set<ResourceLocation> removed = new LinkedHashSet<>();

    public ResearchKubeEvent(Map<ResourceLocation, JsonObject> existing) {
        this.existing = existing;
    }

    private static ResourceLocation id(String raw) {
        return raw.indexOf(':') >= 0 ? ResourceLocation.parse(raw) : ResourceLocation.fromNamespaceAndPath("kubejs", raw);
    }

    private static ResourceLocation existingId(String raw) {
        return raw.indexOf(':') >= 0 ? ResourceLocation.parse(raw) : AstralSorceryKJS.as(raw);
    }

    public ResearchNodeJS create(String key) {
        ResourceLocation id = id(key);
        removed.remove(id);
        ResearchNodeJS node = ResearchNodeJS.create(id);
        created.put(id, node);
        return node;
    }

    public ResearchNodeJS modify(String key) {
        ResourceLocation id = existingId(key);

        if (created.containsKey(id)) {
            return created.get(id);
        }

        if (modified.containsKey(id)) {
            return modified.get(id);
        }

        JsonObject json = existing.get(id);

        if (json == null) {
            throw new IllegalArgumentException("There is no research node '" + id + "' to modify. Existing: " + existing.keySet());
        }

        ResearchNodeJS node = new ResearchNodeJS(id, json.deepCopy());
        modified.put(id, node);
        return node;
    }

    public ResearchKubeEvent modify(String key, Consumer<ResearchNodeJS> modifier) {
        modifier.accept(modify(key));
        return this;
    }

    public ResearchKubeEvent remove(String... keys) {
        for (String key : keys) {
            ResourceLocation id = existingId(key);
            removed.add(id);
            created.remove(id);
            modified.remove(id);
        }

        return this;
    }

    public boolean exists(String key) {
        ResourceLocation id = existingId(key);
        return created.containsKey(id) || (existing.containsKey(id) && !removed.contains(id));
    }

    public List<String> getIds() {
        List<String> ids = new ArrayList<>();
        existing.keySet().forEach(id -> ids.add(id.toString()));
        created.keySet().forEach(id -> ids.add(id.toString()));
        return ids;
    }
}
