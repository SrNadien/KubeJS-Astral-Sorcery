package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.common.tile.TileAltar;
import hellfirepvp.astralsorcery.common.util.data.ObserverRegistryObject;
import nadiendev.kubejsastralsorcery.altar.KubeAltarBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileAltar.class, remap = false)
public class TileAltarMixin {
    @Inject(method = "getRequiredObserver", at = @At("HEAD"), cancellable = true)
    private void kubejsastralsorcery$customStructure(CallbackInfoReturnable<ObserverRegistryObject> cir) {
        if (((TileAltar) (Object) this).getBlockState().getBlock() instanceof KubeAltarBlock block) {
            cir.setReturnValue(block.getObserver());
        }
    }
}
