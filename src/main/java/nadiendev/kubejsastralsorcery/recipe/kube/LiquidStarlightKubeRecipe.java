package nadiendev.kubejsastralsorcery.recipe.kube;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.rhino.Context;
import hellfirepvp.astralsorcery.common.recipe.liquid.output.LiquidStarlightRecipeOutputModifier;

import static nadiendev.kubejsastralsorcery.recipe.schema.AstralSchemas.*;

public class LiquidStarlightKubeRecipe extends AstralKubeRecipe {
    public KubeRecipe with(Context cx, Object ingredient) {
        return addWrapped(cx, STARLIGHT_OTHER, ingredient);
    }

    public KubeRecipe drop(Context cx, Object item) {
        return addWrapped(cx, STARLIGHT_OUTPUTS, item);
    }

    public KubeRecipe modifier(Context cx, Object modifier) {
        return addWrapped(cx, STARLIGHT_OUTPUTS, modifier);
    }

    private KubeRecipe unit(Context cx, String type) {
        return addTo(STARLIGHT_OUTPUTS, parse(cx, LiquidStarlightRecipeOutputModifier.CODEC, typed(type)));
    }

    public KubeRecipe mergeCrystal(Context cx) {
        return unit(cx, "merge_crystal");
    }

    public KubeRecipe formCrystalCluster(Context cx) {
        return unit(cx, "form_crystal_cluster");
    }

    public KubeRecipe formGemCrystalCluster(Context cx) {
        return unit(cx, "form_gem_crystal_cluster");
    }

    public KubeRecipe growSize(Context cx) {
        return unit(cx, "grow_size");
    }

    public KubeRecipe bindLumen(Context cx) {
        return unit(cx, "bind_lumen");
    }

    public KubeRecipe fillLumen(Context cx) {
        return unit(cx, "fill_lumen");
    }
}
