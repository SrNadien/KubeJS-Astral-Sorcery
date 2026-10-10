package nadiendev.kubejsastralsorcery.recipe.component;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.IngredientWrapper;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.SizedIngredientWrapper;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.match.ItemMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.Map;

public record SizedIngredientBridgeComponent(RecipeComponentType<?> type) implements RecipeComponent<SizedIngredient> {
    @Override
    public Codec<SizedIngredient> codec() {
        return SizedIngredient.NESTED_CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return SizedIngredientWrapper.TYPE_INFO.or(IngredientWrapper.TYPE_INFO).or(TypeInfo.RAW_MAP);
    }

    @Override
    public boolean hasPriority(RecipeMatchContext cx, Object from) {
        return IngredientWrapper.isIngredientLike(from);
    }

    @Override
    public SizedIngredient wrap(RecipeScriptContext cx, Object from) {
        if (from instanceof SizedIngredient sized) {
            return sized;
        }

        if (from instanceof Map<?, ?> || from instanceof JsonObject) {
            JsonObject json = JsonUtils.of(cx.cx(), from).getAsJsonObject();

            if (json.has("ingredient")) {
                return SizedIngredient.NESTED_CODEC.parse(cx.ops().json(), json).getOrThrow();
            }

            return new SizedIngredient(IngredientWrapper.wrap(cx.cx(), from), 1);
        }

        return (SizedIngredient) cx.cx().jsToJava(from, SizedIngredientWrapper.TYPE_INFO);
    }

    @Override
    public boolean matches(RecipeMatchContext cx, SizedIngredient value, ReplacementMatchInfo match) {
        return match.match() instanceof ItemMatch m && !value.ingredient().isEmpty() && m.matches(cx, value.ingredient(), match.exact());
    }

    @Override
    public SizedIngredient replace(RecipeScriptContext cx, SizedIngredient original, ReplacementMatchInfo match, Object with) {
        if (!matches(cx, original, match)) {
            return original;
        }

        Ingredient replacement = wrap(cx, with).ingredient();
        return new SizedIngredient(replacement, original.count());
    }

    @Override
    public boolean isEmpty(SizedIngredient value) {
        return value == null || value.ingredient().isEmpty();
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, SizedIngredient value) {
        var first = IngredientWrapper.first(value.ingredient());

        if (!first.isEmpty()) {
            builder.append(BuiltInRegistries.ITEM.getKey(first.getItem()));
        }
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
