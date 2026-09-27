package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.common.integration.IntegrationCurios;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(value = IntegrationCurios.class, remap = false)
public class IntegrationCuriosMixin {
    @Inject(method = "getCurio", at = @At("HEAD"), cancellable = true)
    private static void kubejsastralsorcery$withoutCurios(LivingEntity entity, Predicate<ItemStack> filter, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!ModList.get().isLoaded("curios")) {
            cir.setReturnValue(new ArrayList<>());
        }
    }
}
