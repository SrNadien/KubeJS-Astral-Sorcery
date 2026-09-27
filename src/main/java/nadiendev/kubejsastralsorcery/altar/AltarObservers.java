package nadiendev.kubejsastralsorcery.altar;

import hellfirepvp.astralsorcery.common.util.data.ObserverRegistryObject;
import hellfirepvp.observerlib.api.ObserverProvider;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class AltarObservers {
    private AltarObservers() {
    }

    public static ObserverRegistryObject of(ResourceLocation id) {
        DeferredHolder<ObserverProvider<?>, ObserverProvider<?>> holder = DeferredHolder.create(RegistryProviders.REGISTRY_KEY, id);
        return new ObserverRegistryObject(holder);
    }
}
