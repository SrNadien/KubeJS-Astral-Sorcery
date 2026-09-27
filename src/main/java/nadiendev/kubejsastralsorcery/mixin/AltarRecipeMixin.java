package nadiendev.kubejsastralsorcery.mixin;

import com.mojang.serialization.MapCodec;
import hellfirepvp.astralsorcery.common.recipe.altar.AltarCraftingInput;
import hellfirepvp.astralsorcery.common.recipe.altar.AltarRecipe;
import nadiendev.kubejsastralsorcery.altar.AltarRestrictions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AltarRecipe.class, remap = false)
public class AltarRecipeMixin {
    @Shadow
    @Final
    @Mutable
    public static MapCodec<AltarRecipe> CODEC;

    @Shadow
    @Final
    @Mutable
    public static StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> STREAM_CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kubejsastralsorcery$wrapCodecs(CallbackInfo ci) {
        CODEC = AltarRestrictions.wrap(CODEC);
        STREAM_CODEC = AltarRestrictions.wrap(STREAM_CODEC);
    }

    @Inject(method = "matches(Lhellfirepvp/astralsorcery/common/recipe/altar/AltarCraftingInput;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true)
    private void kubejsastralsorcery$restrict(AltarCraftingInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (!AltarRestrictions.allows((AltarRecipe) (Object) this, input.getAltar())) {
            cir.setReturnValue(false);
        }
    }
}
