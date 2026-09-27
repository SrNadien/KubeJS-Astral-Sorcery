package nadiendev.kubejsastralsorcery.recipe;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeTypeFunction;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.filter.RecipeFilter;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.Context;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

public class AstralRecipesKubeEvent implements KubeEvent {
    private final RecipesKubeEvent event;

    public final RecipeTypeFunction altar_crafting;
    public final RecipeTypeFunction altar;
    public final RecipeTypeFunction focal_combine;
    public final RecipeTypeFunction combine;
    public final RecipeTypeFunction focal_transmutation;
    public final RecipeTypeFunction transmutation;
    public final RecipeTypeFunction lumen_generation;
    public final RecipeTypeFunction lumen;
    public final RecipeTypeFunction lumen_crystallization;
    public final RecipeTypeFunction crystallization;
    public final RecipeTypeFunction lightwell;
    public final RecipeTypeFunction infusion;
    public final RecipeTypeFunction liquid_starlight;
    public final RecipeTypeFunction liquid_interaction;

    public AstralRecipesKubeEvent(RecipesKubeEvent event) {
        this.event = event;
        this.altar_crafting = event.getRecipeFunction("astralsorcery:altar_crafting");
        this.altar = altar_crafting;
        this.focal_combine = event.getRecipeFunction("astralsorcery:focal_combine");
        this.combine = focal_combine;
        this.focal_transmutation = event.getRecipeFunction("astralsorcery:focal_transmutation");
        this.transmutation = focal_transmutation;
        this.lumen_generation = event.getRecipeFunction("astralsorcery:lumen_generation");
        this.lumen = lumen_generation;
        this.lumen_crystallization = event.getRecipeFunction("astralsorcery:lumen_crystallization");
        this.crystallization = lumen_crystallization;
        this.lightwell = event.getRecipeFunction("astralsorcery:lightwell");
        this.infusion = event.getRecipeFunction("astralsorcery:infusion");
        this.liquid_starlight = event.getRecipeFunction("astralsorcery:liquid_starlight");
        this.liquid_interaction = event.getRecipeFunction("astralsorcery:liquid_interaction");
    }

    public Map<String, Object> getRecipes() {
        return event.getRecipes();
    }

    public void remove(Context cx, RecipeFilter filter) {
        event.remove(cx, filter);
    }

    public void replaceInput(Context cx, RecipeFilter filter, ReplacementMatchInfo match, Object with) {
        event.replaceInput(cx, filter, match, with);
    }

    public void replaceOutput(Context cx, RecipeFilter filter, ReplacementMatchInfo match, Object with) {
        event.replaceOutput(cx, filter, match, with);
    }

    public void forEachRecipe(Context cx, RecipeFilter filter, Consumer<KubeRecipe> consumer) {
        event.forEachRecipe(cx, filter, consumer);
    }

    public Collection<KubeRecipe> findRecipes(Context cx, RecipeFilter filter) {
        return event.findRecipes(cx, filter);
    }

    public Collection<ResourceLocation> findRecipeIds(Context cx, RecipeFilter filter) {
        return event.findRecipeIds(cx, filter);
    }

    public boolean containsRecipe(Context cx, RecipeFilter filter) {
        return event.containsRecipe(cx, filter);
    }

    public KubeRecipe custom(Context cx, JsonObject json) {
        return event.custom(cx, json);
    }
}
