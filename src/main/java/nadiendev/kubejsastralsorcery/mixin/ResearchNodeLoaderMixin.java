package nadiendev.kubejsastralsorcery.mixin;

import com.google.gson.JsonElement;
import hellfirepvp.astralsorcery.common.research.data.ResearchNodeLoader;
import nadiendev.kubejsastralsorcery.research.ResearchInjector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = ResearchNodeLoader.class, remap = false)
public class ResearchNodeLoaderMixin {
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void kubejsastralsorcery$inject(Map<ResourceLocation, JsonElement> dataMap, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        ResearchInjector.inject(dataMap);
    }
}
