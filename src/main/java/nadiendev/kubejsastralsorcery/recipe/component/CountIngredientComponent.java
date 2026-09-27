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
import hellfirepvp.astralsorcery.common.util.data.CountIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.Map;

public record CountIngredientComponent(RecipeComponentType<?> type, Codec<CountIngredient> codec, boolean allowEmpty) implements RecipeComponent<CountIngredient> {
    @Override
    public TypeInfo typeInfo() {
        return SizedIngredientWrapper.TYPE_INFO.or(IngredientWrapper.TYPE_INFO).or(TypeInfo.RAW_MAP);
    }

    @Override
    public boolean hasPriority(RecipeMatchContext cx, Object from) {
        return IngredientWrapper.isIngredientLike(from);
    }

    @Override
    public CountIngredient wrap(RecipeScriptContext cx, Object from) {
        if (from instanceof CountIngredient c) {
            return c;
        }

        if (from instanceof Map<?, ?> || from instanceof JsonObject) {
            JsonObject json = JsonUtils.of(cx.cx(), from).getAsJsonObject();

            if (json.has("ingredient")) {
                return codec.parse(cx.ops().json(), json).getOrThrow();
            }
        }

        SizedIngredient sized = (SizedIngredient) cx.cx().jsToJava(from, SizedIngredientWrapper.TYPE_INFO);
        return new CountIngredient(sized.ingredient(), sized.count());
    }

    @Override
    public boolean matches(RecipeMatchContext cx, CountIngredient value, ReplacementMatchInfo match) {
        return match.match() instanceof ItemMatch m && !value.ingredient().isEmpty() && m.matches(cx, value.ingredient(), match.exact());
    }

    @Override
    public CountIngredient replace(RecipeScriptContext cx, CountIngredient original, ReplacementMatchInfo match, Object with) {
        if (!matches(cx, original, match)) {
            return original;
        }

        CountIngredient replacement = wrap(cx, with);
        return new CountIngredient(replacement.ingredient(), original.count());
    }

    @Override
    public boolean isEmpty(CountIngredient value) {
        return value == null || value.ingredient().isEmpty();
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, CountIngredient value) {
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
