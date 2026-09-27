package nadiendev.kubejsastralsorcery.altar;

import dev.latvian.mods.kubejs.event.KubeEvent;
import hellfirepvp.astralsorcery.common.research.ResearchTier;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import dev.latvian.mods.kubejs.script.ScriptType;
import nadiendev.kubejsastralsorcery.AstralSorceryEvents;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RealAltarTiers {
    private static final Map<String, Definition> DEFINITIONS = new LinkedHashMap<>();
    private static volatile boolean injected;

    private RealAltarTiers() {
    }

    public static synchronized List<Definition> definitions() {
        return Collections.unmodifiableList(new ArrayList<>(DEFINITIONS.values()));
    }

    public static synchronized Definition get(String name) {
        return DEFINITIONS.get(name.toLowerCase(Locale.ROOT));
    }

    private static boolean posted;

    public static synchronized void ensureDefinitionsLoaded() {
        if (posted || !AstralSorceryEvents.ALTAR_TIERS.hasListeners()) {
            return;
        }

        posted = true;

        try {
            AstralSorceryEvents.ALTAR_TIERS.post(ScriptType.STARTUP, new AltarTierKubeEvent());
        } catch (Exception ex) {
            AstralSorceryKJS.LOGGER.error("AstralSorceryEvents.altarTiers failed", ex);
        }
    }

    public static void markInjected() {
        injected = true;
    }

    public static boolean isInjected() {
        return injected;
    }

    public static TileAltar.AltarType visualBase(TileAltar.AltarType type) {
        if (type == null || type.ordinal() < 4) {
            return type;
        }

        Definition def = get(type.name());

        if (def == null) {
            return TileAltar.AltarType.RADIANCE;
        }

        for (TileAltar.AltarType t : TileAltar.AltarType.values()) {
            if (t.name().equalsIgnoreCase(def.base)) {
                return t;
            }
        }

        return TileAltar.AltarType.RADIANCE;
    }

    public static final class Definition {
        public final String name;
        public String base = "radiance";
        public String researchTier = "radiance";
        public ResourceLocation block;

        Definition(String name) {
            this.name = name;
        }

        public ResearchTier resolveResearchTier() {
            return ResearchTier.valueOf(researchTier.toUpperCase(Locale.ROOT));
        }
    }

    public static class DefinitionJS {
        private final Definition definition;

        DefinitionJS(Definition definition) {
            this.definition = definition;
        }

        public DefinitionJS base(String tier) {
            String t = tier.toLowerCase(Locale.ROOT);

            if (!t.equals("illumination") && !t.equals("resonance") && !t.equals("luminance") && !t.equals("radiance")) {
                throw new IllegalArgumentException("Base tier must be illumination, resonance, luminance or radiance");
            }

            definition.base = t;
            return this;
        }

        public DefinitionJS researchTier(String tier) {
            ResearchTier.valueOf(tier.toUpperCase(Locale.ROOT));
            definition.researchTier = tier.toLowerCase(Locale.ROOT);
            return this;
        }

        public DefinitionJS block(String block) {
            definition.block = ResourceLocation.parse(block);
            return this;
        }
    }

    public static class AltarTierKubeEvent implements KubeEvent {
        public DefinitionJS create(String name) {
            String n = name.toLowerCase(Locale.ROOT);

            if (!n.matches("[a-z][a-z0-9_]*")) {
                throw new IllegalArgumentException("Altar tier names must be lowercase letters, digits and underscores: '" + name + "'");
            }

            if (List.of("illumination", "resonance", "luminance", "radiance").contains(n)) {
                throw new IllegalArgumentException("Altar tier '" + n + "' already exists");
            }

            if (injected) {
                AstralSorceryKJS.LOGGER.error("Altar tier '{}' was declared after the altar tiers were already built. Real tiers only work from a startup script.", n);
            }

            Definition def;

            synchronized (RealAltarTiers.class) {
                def = DEFINITIONS.computeIfAbsent(n, Definition::new);
            }

            return new DefinitionJS(def);
        }
    }
}
