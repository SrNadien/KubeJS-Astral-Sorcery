package nadiendev.kubejsastralsorcery.recipe.kube;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.rhino.Context;

import static nadiendev.kubejsastralsorcery.recipe.schema.AstralSchemas.*;

public class FocalCombineKubeRecipe extends AstralKubeRecipe {
    public KubeRecipe input(Context cx, Object ingredient) {
        return addWrapped(cx, COMBINE_INPUTS, ingredient);
    }

    public KubeRecipe output(Context cx, Object item) {
        return addWrapped(cx, COMBINE_OUTPUTS, item);
    }
}
