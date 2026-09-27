package nadiendev.kubejsastralsorcery;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;
import dev.latvian.mods.kubejs.registry.RegistryKubeEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import nadiendev.kubejsastralsorcery.altar.RealAltarTiers;
import nadiendev.kubejsastralsorcery.config.AstralConfigKubeEvent;
import nadiendev.kubejsastralsorcery.focal.FocalPointKubeEvent;
import nadiendev.kubejsastralsorcery.focal.FocalPointTweaks;
import nadiendev.kubejsastralsorcery.lumen.LumenBindingKubeEvent;
import nadiendev.kubejsastralsorcery.recipe.AstralRecipesKubeEvent;
import nadiendev.kubejsastralsorcery.research.ResearchKubeEvent;

public interface AstralSorceryEvents {
    EventGroup GROUP = EventGroup.of("AstralSorcery");

    EventHandler RESEARCH = GROUP.server("research", () -> ResearchKubeEvent.class);
    EventHandler LUMEN_BINDINGS = GROUP.server("lumenBindings", () -> LumenBindingKubeEvent.class);
    EventHandler CONFIG = GROUP.server("config", () -> AstralConfigKubeEvent.class);
    EventHandler FOCAL_POINT = GROUP.server("focalPoint", () -> FocalPointKubeEvent.class);
    EventHandler ALTAR_TIERS = GROUP.startup("altarTiers", () -> RealAltarTiers.AltarTierKubeEvent.class);
    EventHandler RECIPES = GROUP.server("recipes", () -> AstralRecipesKubeEvent.class);
    TargetedEventHandler<String> REGISTRY = GROUP.startup("registry", () -> RegistryKubeEvent.class).requiredTarget(EventTargetType.STRING);

    static void postServerTweaks() {
        FocalPointTweaks.reset();

        try {
            if (CONFIG.hasListeners()) {
                CONFIG.post(ScriptType.SERVER, new AstralConfigKubeEvent());
            }

            if (FOCAL_POINT.hasListeners()) {
                FOCAL_POINT.post(ScriptType.SERVER, new FocalPointKubeEvent());
            }
        } catch (Exception ex) {
            AstralSorceryKJS.LOGGER.error("Failed to apply Astral Sorcery config/focal point scripts", ex);
        }

        FocalPointTweaks.applyFlightBox();
    }
}
