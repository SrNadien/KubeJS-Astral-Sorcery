package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.common.event.helper.TemporaryFlightHelper;
import nadiendev.kubejsastralsorcery.focal.FocalPointTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = TemporaryFlightHelper.class, remap = false)
public class TemporaryFlightHelperMixin {
    @ModifyVariable(method = "allowFlight(Lnet/minecraft/world/entity/player/Player;I)Z", at = @At("HEAD"), argsOnly = true)
    private static int kubejsastralsorcery$focusDuration(int timeout) {
        return timeout == FocalPointTweaks.DEFAULT_FLIGHT_DURATION ? FocalPointTweaks.flightDuration : timeout;
    }
}
