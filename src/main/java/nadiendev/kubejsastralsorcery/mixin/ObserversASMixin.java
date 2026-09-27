package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.common.lib.ObserversAS;
import hellfirepvp.astralsorcery.common.util.data.ObserverRegistryObject;
import hellfirepvp.observerlib.api.ObserverProvider;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import nadiendev.kubejsastralsorcery.altar.AltarObservers;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = ObserversAS.class, remap = false)
public class ObserversASMixin {
    @Inject(method = "getByName", at = @At("RETURN"), cancellable = true)
    private static void kubejsastralsorcery$fallback(ResourceKey<ObserverProvider<?>> key, CallbackInfoReturnable<Optional<ObserverRegistryObject>> cir) {
        if (cir.getReturnValue().isEmpty() && RegistryProviders.getRegistry().containsKey(key.location())) {
            cir.setReturnValue(Optional.of(AltarObservers.of(key.location())));
        }
    }
}
