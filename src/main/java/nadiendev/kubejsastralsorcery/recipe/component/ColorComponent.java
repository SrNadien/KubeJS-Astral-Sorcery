package nadiendev.kubejsastralsorcery.recipe.component;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;

public record ColorComponent(RecipeComponentType<?> type) implements RecipeComponent<Integer> {
    @Override
    public Codec<Integer> codec() {
        return Codec.INT;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.NUMBER.or(TypeInfo.STRING);
    }

    @Override
    public Integer wrap(RecipeScriptContext cx, Object from) {
        return parse(from);
    }

    public static int parse(Object from) {
        if (from instanceof Number number) {
            long value = number.longValue();
            return (int) (value <= 0xFFFFFFL && value >= 0 ? value | 0xFF000000L : value);
        }

        String s = String.valueOf(from).trim();

        if (s.startsWith("#")) {
            s = s.substring(1);
        } else if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
        }

        long value = Long.parseLong(s, 16);
        return (int) (s.length() <= 6 ? value | 0xFF000000L : value);
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
