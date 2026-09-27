package nadiendev.kubejsastralsorcery.recipe.kube;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.util.TinyMap;
import dev.latvian.mods.rhino.Context;
import hellfirepvp.astralsorcery.common.lumen.Lumen;

import java.util.ArrayList;
import java.util.List;

import static nadiendev.kubejsastralsorcery.recipe.schema.AstralSchemas.*;

public class LumenGenerationKubeRecipe extends AstralKubeRecipe {
    public KubeRecipe combine(Context cx, Object lumen, int amount) {
        Lumen value = wrapWith(cx, LUMEN, lumen);
        TinyMap<Lumen, Integer> current = getValue(GENERATION_COMBINATION);
        List<TinyMap.Entry<Lumen, Integer>> entries = new ArrayList<>();

        if (current != null) {
            for (TinyMap.Entry<Lumen, Integer> entry : current.entries()) {
                if (entry.key() != value) {
                    entries.add(entry);
                }
            }
        }

        entries.add(new TinyMap.Entry<>(value, amount));
        return setValue(GENERATION_COMBINATION, new TinyMap<>(entries));
    }
}
