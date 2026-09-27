package nadiendev.kubejsastralsorcery.recipe.component;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.function.BiFunction;

public record CodecComponent<T>(RecipeComponentType<?> type, Codec<T> codec, Class<?> valueClass, TypeInfo typeInfo, BiFunction<RecipeScriptContext, Object, T> shortcut) implements RecipeComponent<T> {
    public static <T> CodecComponent<T> of(RecipeComponentType<?> type, Codec<T> codec, Class<?> valueClass) {
        return new CodecComponent<>(type, codec, valueClass, TypeInfo.of(valueClass).or(TypeInfo.RAW_MAP), (cx, from) -> null);
    }

    public static <T> CodecComponent<T> of(RecipeComponentType<?> type, Codec<T> codec, Class<?> valueClass, BiFunction<RecipeScriptContext, Object, T> shortcut) {
        return new CodecComponent<>(type, codec, valueClass, TypeInfo.of(valueClass).or(TypeInfo.RAW_MAP).or(TypeInfo.STRING), shortcut);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T wrap(RecipeScriptContext cx, Object from) {
        if (valueClass.isInstance(from)) {
            return (T) from;
        }

        T quick = shortcut.apply(cx, from);

        if (quick != null) {
            return quick;
        }

        return codec.parse(cx.ops().json(), JsonUtils.of(cx.cx(), from)).getOrThrow();
    }

    @Override
    public boolean isEmpty(T value) {
        return value == null;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
