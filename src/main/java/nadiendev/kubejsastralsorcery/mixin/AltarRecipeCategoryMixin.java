package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.common.integration.jei.category.AltarRecipeCategory;
import hellfirepvp.astralsorcery.common.recipe.altar.AltarRecipe;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import nadiendev.kubejsastralsorcery.altar.RealAltarTiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AltarRecipeCategory.class, remap = false)
public class AltarRecipeCategoryMixin {
    @Redirect(method = "getBackground", at = @At(value = "INVOKE", target = "Lhellfirepvp/astralsorcery/common/recipe/altar/AltarRecipe;getRequiredType()Lhellfirepvp/astralsorcery/common/tile/TileAltar$AltarType;"), require = 0)
    private TileAltar.AltarType kubejsastralsorcery$jeiTier(AltarRecipe recipe) {
        return RealAltarTiers.visualBase(recipe.getRequiredType());
    }
}
