package nadiendev.kubejsastralsorcery.focal;

import hellfirepvp.astralsorcery.common.constellation.BaseConstellation;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import nadiendev.kubejsastralsorcery.mixin.TileStarlightFocusCrystalAccessor;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class FocalPointTweaks {
    public static final float DEFAULT_FLIGHT_SIZE = 17F;
    public static final int DEFAULT_FLIGHT_DURATION = 60;
    public static final int DEFAULT_LINK_DISTANCE = 16;

    public static volatile float flightSize = DEFAULT_FLIGHT_SIZE;
    public static volatile int flightDuration = DEFAULT_FLIGHT_DURATION;
    public static volatile boolean flightEnabled = true;
    public static volatile float starlightMultiplier = 1F;
    public static volatile float layerBonus = 0F;
    public static volatile int linkDistance = DEFAULT_LINK_DISTANCE;
    public static volatile float transmutationSpeed = 1F;
    public static final Map<ResourceLocation, Float> CONSTELLATION_MULTIPLIERS = new HashMap<>();

    private FocalPointTweaks() {
    }

    public static void reset() {
        flightSize = DEFAULT_FLIGHT_SIZE;
        flightDuration = DEFAULT_FLIGHT_DURATION;
        flightEnabled = true;
        starlightMultiplier = 1F;
        layerBonus = 0F;
        linkDistance = DEFAULT_LINK_DISTANCE;
        transmutationSpeed = 1F;

        synchronized (CONSTELLATION_MULTIPLIERS) {
            CONSTELLATION_MULTIPLIERS.clear();
        }

        applyFlightBox();
    }

    public static void applyFlightBox() {
        float size = flightEnabled ? flightSize : 0F;
        TileStarlightFocusCrystalAccessor.kubejsastralsorcery$setPlayerBox(AABB.ofSize(Vec3.atCenterOf(Vec3i.ZERO), size, size, size));
    }

    public static float multiplierFor(BaseConstellation constellation, int validLayers) {
        float value = starlightMultiplier * (1F + layerBonus * validLayers);
        ResourceLocation id = RegistriesAS.REGISTRY_CONSTELLATIONS.getKey(constellation);

        if (id != null) {
            synchronized (CONSTELLATION_MULTIPLIERS) {
                value *= CONSTELLATION_MULTIPLIERS.getOrDefault(id, 1F);
            }
        }

        return value;
    }
}
