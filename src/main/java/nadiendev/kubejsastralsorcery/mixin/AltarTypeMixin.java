package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.common.tile.TileAltar;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import nadiendev.kubejsastralsorcery.altar.AltarObservers;
import nadiendev.kubejsastralsorcery.altar.RealAltarTiers;
import net.minecraft.core.registries.BuiltInRegistries;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(value = TileAltar.AltarType.class, remap = false)
public abstract class AltarTypeMixin {
    @Shadow
    @Final
    @Mutable
    private static TileAltar.AltarType[] $VALUES;

    @Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lhellfirepvp/astralsorcery/common/tile/TileAltar$AltarType;$VALUES:[Lhellfirepvp/astralsorcery/common/tile/TileAltar$AltarType;", opcode = Opcodes.PUTSTATIC, shift = At.Shift.AFTER))
    private static void kubejsastralsorcery$addTiers(CallbackInfo ci) {
        RealAltarTiers.ensureDefinitionsLoaded();
        List<TileAltar.AltarType> values = new ArrayList<>(Arrays.asList($VALUES));

        for (RealAltarTiers.Definition def : RealAltarTiers.definitions()) {
            TileAltar.AltarType base = null;

            for (TileAltar.AltarType type : $VALUES) {
                if (type.name().equalsIgnoreCase(def.base)) {
                    base = type;
                }
            }

            if (base == null || def.block == null) {
                AstralSorceryKJS.LOGGER.error("Skipping altar tier '{}': it needs a base tier and a block", def.name);
                continue;
            }

            TileAltar.AltarType type = AltarTypeInvoker.kubejsastralsorcery$create(def.name.toUpperCase(), values.size(), def.resolveResearchTier(),
                AltarObservers.of(def.block), () -> BuiltInRegistries.BLOCK.get(def.block), base.getShape(), base.getContainerType());
            values.add(type);
            AstralSorceryKJS.LOGGER.info("Added altar tier {} (ordinal {}, based on {})", type.name(), type.ordinal(), base.name());
        }

        $VALUES = values.toArray(new TileAltar.AltarType[0]);
        RealAltarTiers.markInjected();
    }
}
