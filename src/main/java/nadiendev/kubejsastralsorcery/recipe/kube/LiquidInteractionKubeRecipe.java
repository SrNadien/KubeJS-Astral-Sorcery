package nadiendev.kubejsastralsorcery.recipe.kube;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.rhino.Context;
import hellfirepvp.astralsorcery.common.recipe.liquid.interaction.result.LiquidInteractionResult;

import static nadiendev.kubejsastralsorcery.recipe.schema.AstralSchemas.*;

public class LiquidInteractionKubeRecipe extends AstralKubeRecipe {
    public KubeRecipe spawnEntity(Context cx, String entityType) {
        JsonObject json = typed("spawn_entity");
        json.addProperty("entityType", entityType.indexOf(':') >= 0 ? entityType : "minecraft:" + entityType);
        return setValue(INTERACTION_RESULT, parse(cx, LiquidInteractionResult.CODEC, json));
    }

    public KubeRecipe drop(Context cx, Object item) {
        return setValue(INTERACTION_RESULT, wrapWith(cx, INTERACTION_RESULT.component, item));
    }
}
