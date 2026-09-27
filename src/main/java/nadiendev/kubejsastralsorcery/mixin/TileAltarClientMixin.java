package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.common.tile.TileAltar;
import nadiendev.kubejsastralsorcery.altar.RealAltarTiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileAltar.class, remap = false)
public class TileAltarClientMixin {
    @Redirect(method = "playCraftingSound", at = @At(value = "INVOKE", target = "Lhellfirepvp/astralsorcery/common/tile/TileAltar$Data;getAltarType()Lhellfirepvp/astralsorcery/common/tile/TileAltar$AltarType;"))
    private TileAltar.AltarType kubejsastralsorcery$soundTier(TileAltar.Data data) {
        return RealAltarTiers.visualBase(data.getAltarType());
    }
}
