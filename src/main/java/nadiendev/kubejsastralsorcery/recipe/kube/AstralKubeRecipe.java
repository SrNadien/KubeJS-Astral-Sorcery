package nadiendev.kubejsastralsorcery.recipe.kube;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.ListRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;

import java.util.ArrayList;
import java.util.List;

public class AstralKubeRecipe extends KubeRecipe {
    protected RecipeScriptContext scx(Context cx) {
        return new RecipeScriptContext.Impl(cx, this);
    }

    protected <T> T wrapWith(Context cx, RecipeComponent<T> component, Object from) {
        return component.wrap(scx(cx), from);
    }

    @SuppressWarnings("unchecked")
    protected <T> T wrapElement(Context cx, RecipeKey<List<T>> key, Object from) {
        RecipeComponent<List<T>> component = key.component;

        if (component instanceof ListRecipeComponent<?> list) {
            return ((RecipeComponent<T>) list.component()).wrap(scx(cx), from);
        }

        throw new IllegalStateException("Key " + key.name + " is not a list");
    }

    protected <T> KubeRecipe addTo(RecipeKey<List<T>> key, T value) {
        List<T> current = getValue(key);
        List<T> list = current == null ? new ArrayList<>() : new ArrayList<>(current);
        list.add(value);
        return setValue(key, list);
    }

    protected <T> KubeRecipe addWrapped(Context cx, RecipeKey<List<T>> key, Object from) {
        return addTo(key, wrapElement(cx, key, from));
    }

    protected <T> T parse(Context cx, Codec<T> codec, JsonObject json) {
        return codec.parse(scx(cx).ops().json(), json).getOrThrow();
    }

    protected static JsonObject typed(String type) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type.indexOf(':') >= 0 ? type : "astralsorcery:" + type);
        return json;
    }

    protected static List<String> normalizePattern(List<String> pattern, int size) {
        List<String> result = new ArrayList<>(size);

        for (int row = 0; row < size; row++) {
            String line = pattern != null && row < pattern.size() ? pattern.get(row) : "";

            if (line.length() > size) {
                throw new IllegalArgumentException("Pattern row '" + line + "' is longer than " + size + " characters");
            }

            result.add(line + " ".repeat(size - line.length()));
        }

        if (pattern != null && pattern.size() > size) {
            throw new IllegalArgumentException("Pattern has more than " + size + " rows");
        }

        return result;
    }
}
