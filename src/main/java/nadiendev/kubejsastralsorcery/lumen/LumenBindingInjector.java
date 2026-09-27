package nadiendev.kubejsastralsorcery.lumen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.script.ScriptType;
import nadiendev.kubejsastralsorcery.AstralSorceryEvents;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class LumenBindingInjector {
    private LumenBindingInjector() {
    }

    public static void inject(Map<ResourceLocation, JsonElement> dataMap) {
        if (!AstralSorceryEvents.LUMEN_BINDINGS.hasListeners()) {
            return;
        }

        Map<ResourceLocation, JsonObject> bindings = new HashMap<>();
        Map<ResourceLocation, ResourceLocation> mapping = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : dataMap.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }

            if (entry.getKey().getPath().startsWith("_")) {
                entry.getValue().getAsJsonObject().entrySet().forEach(e -> {
                    ResourceLocation lumen = ResourceLocation.tryParse(e.getKey());
                    ResourceLocation binding = ResourceLocation.tryParse(e.getValue().getAsString());

                    if (lumen != null && binding != null) {
                        mapping.put(lumen, binding);
                    }
                });
            } else {
                bindings.put(entry.getKey(), entry.getValue().getAsJsonObject());
            }
        }

        LumenBindingKubeEvent event = new LumenBindingKubeEvent(bindings, mapping);

        try {
            AstralSorceryEvents.LUMEN_BINDINGS.post(ScriptType.SERVER, event);
        } catch (Exception ex) {
            AstralSorceryKJS.LOGGER.error("AstralSorceryEvents.lumenBindings failed", ex);
            return;
        }

        event.removed.forEach(dataMap::remove);
        event.changed.forEach((id, js) -> dataMap.put(id, js.getJson()));

        if (!event.mapping.isEmpty()) {
            for (Map.Entry<ResourceLocation, JsonElement> entry : dataMap.entrySet()) {
                if (entry.getKey().getPath().startsWith("_") && entry.getValue().isJsonObject()) {
                    event.mapping.keySet().forEach(lumen -> entry.getValue().getAsJsonObject().remove(lumen.toString()));
                }
            }

            JsonObject ours = new JsonObject();
            event.mapping.forEach((lumen, binding) -> ours.addProperty(lumen.toString(), binding.toString()));
            dataMap.put(AstralSorceryKJS.id("_kubejs_mapping"), ours);
        }

        AstralSorceryKJS.LOGGER.info("Lumen binding event: {} changed, {} removed, {} remapped", event.changed.size(), event.removed.size(), event.mapping.size());
    }
}
