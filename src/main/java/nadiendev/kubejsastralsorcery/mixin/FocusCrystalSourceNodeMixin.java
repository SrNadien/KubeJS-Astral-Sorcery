package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.common.starlight.transmission.StarlightTransmissionPacket;
import hellfirepvp.astralsorcery.common.tile.network.FocusCrystalSourceNode;
import nadiendev.kubejsastralsorcery.focal.FocalPointTweaks;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = FocusCrystalSourceNode.class, remap = false)
public class FocusCrystalSourceNodeMixin {
    @Inject(method = "produceStarlight", at = @At("RETURN"), cancellable = true)
    private void kubejsastralsorcery$scale(Level level, CallbackInfoReturnable<Optional<StarlightTransmissionPacket>> cir) {
        FocusCrystalSourceNode node = (FocusCrystalSourceNode) (Object) this;
        Optional<StarlightTransmissionPacket> packet = cir.getReturnValue();

        if (packet != null && packet.isPresent()) {
            StarlightTransmissionPacket p = packet.get();
            float multiplier = FocalPointTweaks.multiplierFor(p.constellation(), node.getValidLayers());

            if (multiplier != 1F) {
                cir.setReturnValue(Optional.of(new StarlightTransmissionPacket(p.constellation(), p.amount() * multiplier)));
            }
        }
    }

    @Inject(method = "getMaxBlockLinkDistance", at = @At("HEAD"), cancellable = true)
    private void kubejsastralsorcery$linkDistance(CallbackInfoReturnable<Optional<Integer>> cir) {
        cir.setReturnValue(Optional.of(FocalPointTweaks.linkDistance));
    }
}
