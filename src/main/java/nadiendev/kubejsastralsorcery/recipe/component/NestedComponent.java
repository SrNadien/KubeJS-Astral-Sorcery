package nadiendev.kubejsastralsorcery.recipe.component;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentValue;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentWithParent;

public record NestedComponent<T>(RecipeComponent<T> parentComponent, String objectName) implements RecipeComponentWithParent<T> {
    @Override
    public RecipeComponentType<?> type() {
        return parentComponent.type();
    }

    @Override
    public void writeToJson(KubeRecipe recipe, RecipeComponentValue<T> cv, JsonObject json) {
        JsonObject child = json.has(objectName) && json.get(objectName).isJsonObject() ? json.getAsJsonObject(objectName) : new JsonObject();
        parentComponent.writeToJson(recipe, cv, child);
        json.add(objectName, child);
    }

    @Override
    public void readFromJson(KubeRecipe recipe, RecipeComponentValue<T> cv, JsonObject json) {
        if (json.has(objectName) && json.get(objectName).isJsonObject()) {
            parentComponent.readFromJson(recipe, cv, json.getAsJsonObject(objectName));
        }
    }

    @Override
    public String toString() {
        return parentComponent + "@" + objectName;
    }
}
