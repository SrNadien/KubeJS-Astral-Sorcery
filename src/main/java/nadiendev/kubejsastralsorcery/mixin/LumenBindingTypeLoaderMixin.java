package nadiendev.kubejsastralsorcery.mixin;

import com.google.gson.JsonElement;
import hellfirepvp.astralsorcery.common.lumen.binding.data.LumenBindingTypeLoader;
import nadiendev.kubejsastralsorcery.lumen.LumenBindingInjector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = LumenBindingTypeLoader.class, remap = false)
public class LumenBindingTypeLoaderMixin {
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void kubejsastralsorcery$inject(Map<ResourceLocation, JsonElement> dataMap, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        LumenBindingInjector.inject(dataMap);
    }
}
