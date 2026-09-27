package nadiendev.kubejsastralsorcery.recipe.component;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.IngredientWrapper;
import dev.latvian.mods.kubejs.fluid.FluidWrapper;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.match.FluidMatch;
import dev.latvian.mods.kubejs.recipe.match.ItemMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.type.TypeInfo;
import hellfirepvp.astralsorcery.common.ingredient.IngredientBridge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Map;

public record IngredientBridgeComponent(RecipeComponentType<?> type) implements RecipeComponent<IngredientBridge> {
    @Override
    public Codec<IngredientBridge> codec() {
        return IngredientBridge.CODEC.codec();
    }

    @Override
    public TypeInfo typeInfo() {
        return IngredientWrapper.TYPE_INFO.or(FluidWrapper.SIZED_INGREDIENT_TYPE_INFO).or(TypeInfo.RAW_MAP);
    }

    @Override
    public boolean hasPriority(RecipeMatchContext cx, Object from) {
        return IngredientWrapper.isIngredientLike(from);
    }

    @Override
    public IngredientBridge wrap(RecipeScriptContext cx, Object from) {
        if (from instanceof IngredientBridge bridge) {
            return bridge;
        }

        if (from instanceof SizedFluidIngredient fluid) {
            return IngredientBridge.of(fluid);
        }

        if (from instanceof Map<?, ?> || from instanceof JsonObject) {
            JsonObject json = JsonUtils.of(cx.cx(), from).getAsJsonObject();

            if (json.has("type") && json.has("ingredient")) {
                String t = json.get("type").getAsString();

                if (t.equals("item") || t.equals("fluid")) {
                    return IngredientBridge.CODEC.codec().parse(cx.ops().json(), json).getOrThrow();
                }
            }

            if (json.has("fluid_tag")) {
                json.add("tag", json.remove("fluid_tag"));
            }

            if (json.has("fluid") || (json.has("tag") && json.has("amount"))) {
                if (!json.has("amount")) {
                    json.addProperty("amount", 1000);
                }

                return IngredientBridge.of(SizedFluidIngredient.FLAT_CODEC.parse(cx.ops().json(), json).getOrThrow());
            }
        }

        return IngredientBridge.of(IngredientWrapper.wrap(cx.cx(), from));
    }

    @Override
    public boolean matches(RecipeMatchContext cx, IngredientBridge value, ReplacementMatchInfo match) {
        if (value.isEmpty()) {
            return false;
        }

        return switch (value.getIngredientType()) {
            case ITEM -> match.match() instanceof ItemMatch m && m.matches(cx, value.getIngredient(), match.exact());
            case FLUID -> match.match() instanceof FluidMatch m && m.matches(cx, value.getFluidIngredient().ingredient(), match.exact());
        };
    }

    @Override
    public IngredientBridge replace(RecipeScriptContext cx, IngredientBridge original, ReplacementMatchInfo match, Object with) {
        return matches(cx, original, match) ? wrap(cx, with) : original;
    }

    @Override
    public boolean isEmpty(IngredientBridge value) {
        return value == null || value.isEmpty();
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, IngredientBridge value) {
        if (value.getIngredientType() == IngredientBridge.Type.ITEM) {
            Ingredient in = value.getIngredient();
            var first = IngredientWrapper.first(in);

            if (!first.isEmpty()) {
                builder.append(BuiltInRegistries.ITEM.getKey(first.getItem()));
            }
        }
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
