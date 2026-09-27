package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.common.recipe.focal.place.ActiveFocalTransmutationRecipe;
import nadiendev.kubejsastralsorcery.focal.FocalPointTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ActiveFocalTransmutationRecipe.class, remap = false)
public class ActiveFocalTransmutationRecipeMixin {
    @ModifyVariable(method = "tick(JF)Z", at = @At("HEAD"), argsOnly = true)
    private float kubejsastralsorcery$speed(float reduction) {
        return reduction * FocalPointTweaks.transmutationSpeed;
    }
}
