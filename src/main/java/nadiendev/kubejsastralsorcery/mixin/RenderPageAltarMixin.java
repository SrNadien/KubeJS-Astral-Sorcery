package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.client.screen.tome.page.RenderPageAltar;
import hellfirepvp.astralsorcery.common.recipe.altar.AltarRecipe;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import nadiendev.kubejsastralsorcery.altar.RealAltarTiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderPageAltar.class, remap = false)
public class RenderPageAltarMixin {
    @Redirect(method = "resolveBackgroundTexture", at = @At(value = "INVOKE", target = "Lhellfirepvp/astralsorcery/common/recipe/altar/AltarRecipe;getRequiredType()Lhellfirepvp/astralsorcery/common/tile/TileAltar$AltarType;"))
    private TileAltar.AltarType kubejsastralsorcery$pageTier(AltarRecipe recipe) {
        return RealAltarTiers.visualBase(recipe.getRequiredType());
    }
}
