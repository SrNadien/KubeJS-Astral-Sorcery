package nadiendev.kubejsastralsorcery.recipe.component;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.util.OpsContainer;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public record AstralRegistryComponent<T>(RecipeComponentType<?> type, Registry<T> registry, Class<?> valueClass, String defaultNamespace) implements RecipeComponent<T> {
    @Override
    public Codec<T> codec() {
        return registry.byNameCodec();
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.STRING.or(TypeInfo.of(valueClass));
    }

    @Override
    @SuppressWarnings("unchecked")
    public T wrap(RecipeScriptContext cx, Object from) {
        if (valueClass.isInstance(from)) {
            return (T) from;
        }

        if (from instanceof Holder<?> holder && valueClass.isInstance(holder.value())) {
            return (T) holder.value();
        }

        ResourceLocation id = parseId(String.valueOf(from));
        return registry.getOptional(id).orElseThrow(() -> new IllegalArgumentException("Unknown " + registry.key().location() + " entry '" + id + "'"));
    }

    public ResourceLocation parseId(String raw) {
        String s = raw.trim();
        return s.indexOf(':') >= 0 ? ResourceLocation.parse(s) : ResourceLocation.fromNamespaceAndPath(defaultNamespace, s);
    }

    @Override
    public boolean isEmpty(T value) {
        return value == null;
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, T value) {
        ResourceLocation id = registry.getKey(value);

        if (id != null) {
            builder.append(id);
        }
    }

    @Override
    public String toString(OpsContainer ops, T value) {
        return String.valueOf(registry.getKey(value));
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
