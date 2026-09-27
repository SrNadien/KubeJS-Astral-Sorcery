package nadiendev.kubejsastralsorcery.constellation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryObjectStorage;
import hellfirepvp.astralsorcery.common.constellation.BaseConstellation;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import dev.latvian.mods.kubejs.registry.RegistryKubeEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import nadiendev.kubejsastralsorcery.AstralSorceryEvents;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class ConstellationData {
    private static final float[] AUTO_YAWS = {15F, 70F, 125F, 165F, 205F, 265F, 320F, 350F, 45F, 110F, 185F, 230F, 285F, 335F};
    private static final float[] AUTO_PITCHES = {62F, 24F, 68F, 26F, 64F, 58F, 48F, 22F, 72F, 36F, 76F, 20F, 70F, 38F};

    private ConstellationData() {
    }

    public static List<ConstellationBuilder> builders() {
        List<ConstellationBuilder> list = new ArrayList<>();

        for (BuilderBase<? extends BaseConstellation> builder : RegistryObjectStorage.of(RegistriesAS.KEY_CONSTELLATIONS)) {
            if (builder instanceof ConstellationBuilder c) {
                list.add(c);
            }
        }

        return list;
    }

    public static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(RegistriesAS.KEY_CONSTELLATIONS) && AstralSorceryEvents.REGISTRY.hasListeners("constellations")) {
            AstralSorceryEvents.REGISTRY.post(ScriptType.STARTUP, "constellations", new RegistryKubeEvent<>(RegistriesAS.KEY_CONSTELLATIONS));
        }
    }

    public static void generateData(KubeDataGenerator generator) {
        List<ConstellationBuilder> builders = builders();
        JsonArray focal = new JsonArray();

        for (ConstellationBuilder builder : builders) {
            if (builder.focalPoint) {
                focal.add(builder.id.toString());
            }

            if (builder.rootPerkType != null) {
                generator.json(ResourceLocation.fromNamespaceAndPath(builder.id.getNamespace(), "perks/root_" + builder.id.getPath()), builder.createRootPerk());
            }
        }

        if (!focal.isEmpty()) {
            JsonObject tag = new JsonObject();
            tag.addProperty("replace", false);
            tag.add("values", focal);
            generator.json(AstralSorceryKJS.as("tags/astralsorcery/constellations/may_be_focal_point"), tag);
        }
    }

    public static void generateAssets(KubeAssetGenerator generator) {
        List<ConstellationBuilder> builders = builders();

        if (builders.isEmpty()) {
            return;
        }

        JsonArray positions = new JsonArray();
        int auto = 0;

        for (ConstellationBuilder builder : builders) {
            if (builder.skyPositions.isEmpty()) {
                int i = auto++ % AUTO_YAWS.length;
                positions.add(position(AUTO_YAWS[i], AUTO_PITCHES[i]));
            } else {
                builder.skyPositions.forEach(p -> positions.add(position(p[0], p[1])));
            }
        }

        JsonObject json = new JsonObject();
        json.addProperty("replace", false);
        json.add("positions", positions);
        generator.json(AstralSorceryKJS.id("constellation_positions"), json);
    }

    private static JsonObject position(float yaw, float pitch) {
        JsonObject json = new JsonObject();
        json.addProperty("yaw", yaw);
        json.addProperty("pitch", pitch);
        return json;
    }
}
