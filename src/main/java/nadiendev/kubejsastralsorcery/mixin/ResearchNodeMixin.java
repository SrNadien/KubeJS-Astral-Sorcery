package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.common.research.ResearchNode;
import nadiendev.kubejsastralsorcery.research.ResearchInjector;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ResearchNode.class, remap = false)
public abstract class ResearchNodeMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void kubejsastralsorcery$title(CallbackInfoReturnable<Component> cir) {
        ResearchInjector.title(((ResearchNode) (Object) this).getKey()).ifPresent(title -> cir.setReturnValue(Component.literal(title)));
    }
}
