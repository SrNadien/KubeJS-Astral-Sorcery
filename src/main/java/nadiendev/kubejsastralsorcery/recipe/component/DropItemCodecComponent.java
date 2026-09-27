package nadiendev.kubejsastralsorcery.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.ItemWrapper;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.match.ItemMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public record DropItemCodecComponent<T>(RecipeComponentType<?> type, Codec<T> codec, Class<?> valueClass, String dropType, String itemField) implements RecipeComponent<T> {
    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(valueClass).or(ItemWrapper.TYPE_INFO).or(TypeInfo.RAW_MAP);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T wrap(RecipeScriptContext cx, Object from) {
        if (valueClass.isInstance(from)) {
            return (T) from;
        }

        if (from instanceof Map<?, ?> || from instanceof JsonObject) {
            JsonElement json = JsonUtils.of(cx.cx(), from);

            if (json.isJsonObject() && json.getAsJsonObject().has("type")) {
                return codec.parse(cx.ops().json(), json).getOrThrow();
            }
        }

        ItemStack stack = ItemWrapper.wrap(cx.cx(), from);
        JsonObject json = new JsonObject();
        json.addProperty("type", dropType);
        json.add(itemField, ItemStack.CODEC.encodeStart(cx.ops().json(), stack).getOrThrow());
        return codec.parse(cx.ops().json(), json).getOrThrow();
    }

    public ItemStack droppedItem(RecipeMatchContext cx, T value) {
        JsonElement json = codec.encodeStart(cx.ops().json(), value).result().orElse(null);

        if (json != null && json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();

            if (obj.has("type") && dropType.equals(obj.get("type").getAsString()) && obj.has(itemField)) {
                return ItemStack.CODEC.parse(cx.ops().json(), obj.get(itemField)).result().orElse(ItemStack.EMPTY);
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(RecipeMatchContext cx, T value, ReplacementMatchInfo match) {
        if (match.match() instanceof ItemMatch m) {
            ItemStack stack = droppedItem(cx, value);
            return !stack.isEmpty() && m.matches(cx, stack, match.exact());
        }

        return false;
    }

    @Override
    public T replace(RecipeScriptContext cx, T original, ReplacementMatchInfo match, Object with) {
        if (!matches(cx, original, match)) {
            return original;
        }

        ItemStack old = droppedItem(cx, original);
        ItemStack replacement = ItemWrapper.wrap(cx.cx(), with);

        if (replacement.getCount() == 1 && old.getCount() > 1) {
            replacement = replacement.copyWithCount(old.getCount());
        }

        return wrap(cx, replacement);
    }

    @Override
    public boolean isEmpty(T value) {
        return value == null;
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, T value) {
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
