package nadiendev.kubejsastralsorcery.focal;

import dev.latvian.mods.kubejs.event.KubeEvent;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.minecraft.resources.ResourceLocation;

public class FocalPointKubeEvent implements KubeEvent {
    public FocalPointKubeEvent flightRadius(float radius) {
        FocalPointTweaks.flightSize = Math.max(0F, radius * 2F + 1F);
        return this;
    }

    public FocalPointKubeEvent flightDuration(int ticks) {
        FocalPointTweaks.flightDuration = Math.max(20, ticks);
        return this;
    }

    public FocalPointKubeEvent disableFlight() {
        FocalPointTweaks.flightEnabled = false;
        return this;
    }

    public FocalPointKubeEvent enableFlight() {
        FocalPointTweaks.flightEnabled = true;
        return this;
    }

    public FocalPointKubeEvent starlightMultiplier(float multiplier) {
        FocalPointTweaks.starlightMultiplier = Math.max(0F, multiplier);
        return this;
    }

    public FocalPointKubeEvent constellationMultiplier(String constellation, float multiplier) {
        ResourceLocation id = constellation.indexOf(':') >= 0 ? ResourceLocation.parse(constellation) : AstralSorceryKJS.as(constellation);

        synchronized (FocalPointTweaks.CONSTELLATION_MULTIPLIERS) {
            FocalPointTweaks.CONSTELLATION_MULTIPLIERS.put(id, Math.max(0F, multiplier));
        }

        return this;
    }

    public FocalPointKubeEvent layerBonus(float bonusPerLayer) {
        FocalPointTweaks.layerBonus = bonusPerLayer;
        return this;
    }

    public FocalPointKubeEvent linkDistance(int blocks) {
        FocalPointTweaks.linkDistance = Math.max(1, blocks);
        return this;
    }

    public FocalPointKubeEvent transmutationSpeed(float multiplier) {
        FocalPointTweaks.transmutationSpeed = Math.max(0.01F, multiplier);
        return this;
    }

    public float getFlightRadius() {
        return (FocalPointTweaks.flightSize - 1F) / 2F;
    }

    public int getFlightDuration() {
        return FocalPointTweaks.flightDuration;
    }

    public float getStarlightMultiplier() {
        return FocalPointTweaks.starlightMultiplier;
    }

    public int getLinkDistance() {
        return FocalPointTweaks.linkDistance;
    }
}
