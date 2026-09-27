package nadiendev.kubejsastralsorcery.lumen;

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

public class LumenBindingKubeEvent implements KubeEvent {
    private final Map<ResourceLocation, JsonObject> existing;
    private final Map<ResourceLocation, ResourceLocation> existingMapping;
    final Map<ResourceLocation, LumenBindingJS> changed = new LinkedHashMap<>();
    final Set<ResourceLocation> removed = new LinkedHashSet<>();
    final Map<ResourceLocation, ResourceLocation> mapping = new LinkedHashMap<>();

    public LumenBindingKubeEvent(Map<ResourceLocation, JsonObject> existing, Map<ResourceLocation, ResourceLocation> existingMapping) {
        this.existing = existing;
        this.existingMapping = existingMapping;
    }

    private static ResourceLocation id(String raw) {
        return raw.indexOf(':') >= 0 ? ResourceLocation.parse(raw) : AstralSorceryKJS.as(raw);
    }

    public LumenBindingJS modify(String binding) {
        ResourceLocation id = id(binding);

        if (changed.containsKey(id)) {
            return changed.get(id);
        }

        JsonObject json = existing.get(id);

        if (json == null) {
            throw new IllegalArgumentException("There is no lumen binding '" + id + "'. Existing: " + existing.keySet());
        }

        LumenBindingJS js = new LumenBindingJS(id, json.deepCopy());
        changed.put(id, js);
        return js;
    }

    public LumenBindingKubeEvent modify(String binding, Consumer<LumenBindingJS> modifier) {
        modifier.accept(modify(binding));
        return this;
    }

    public LumenBindingKubeEvent modifyAll(Consumer<LumenBindingJS> modifier) {
        for (ResourceLocation id : new ArrayList<>(existing.keySet())) {
            if (!removed.contains(id)) {
                modifier.accept(modify(id.toString()));
            }
        }

        return this;
    }

    public LumenBindingJS create(String binding) {
        ResourceLocation id = binding.indexOf(':') >= 0 ? ResourceLocation.parse(binding) : ResourceLocation.fromNamespaceAndPath("kubejs", binding);
        removed.remove(id);
        LumenBindingJS js = new LumenBindingJS(id, new JsonObject());
        changed.put(id, js);
        return js;
    }

    public LumenBindingKubeEvent remove(String... bindings) {
        for (String binding : bindings) {
            ResourceLocation id = id(binding);
            removed.add(id);
            changed.remove(id);
        }

        return this;
    }

    public LumenBindingKubeEvent map(String lumen, String binding) {
        mapping.put(id(lumen), binding.indexOf(':') >= 0 ? ResourceLocation.parse(binding) : AstralSorceryKJS.as(binding));
        return this;
    }

    public String getMapping(String lumen) {
        ResourceLocation l = id(lumen);
        ResourceLocation b = mapping.containsKey(l) ? mapping.get(l) : existingMapping.get(l);
        return b == null ? null : b.toString();
    }

    public List<String> getIds() {
        List<String> ids = new ArrayList<>();
        existing.keySet().forEach(id -> ids.add(id.toString()));
        return ids;
    }
}
