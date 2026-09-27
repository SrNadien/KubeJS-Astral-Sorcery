package nadiendev.kubejsastralsorcery.altar;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import hellfirepvp.astralsorcery.common.recipe.altar.AltarRecipe;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Stream;

public final class AltarRestrictions {
    public static final String FIELD = AstralSorceryKJS.MOD_ID + ":altar";
    private static final Map<AltarRecipe, ResourceLocation> REQUIRED = Collections.synchronizedMap(new WeakHashMap<>());

    private AltarRestrictions() {
    }

    public static ResourceLocation requiredAltar(AltarRecipe recipe) {
        return REQUIRED.get(recipe);
    }

    public static boolean allows(AltarRecipe recipe, TileAltar altar) {
        if (altar == null || altar.getLevel() == null) {
            return true;
        }

        Block block = altar.getBlockState().getBlock();
        ResourceLocation required = REQUIRED.get(recipe);

        if (required != null) {
            return required.equals(BuiltInRegistries.BLOCK.getKey(block));
        }

        return !(block instanceof KubeAltarBlock kube && kube.isExclusive());
    }

    public static MapCodec<AltarRecipe> wrap(MapCodec<AltarRecipe> base) {
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.concat(base.keys(ops), Stream.of(ops.createString(FIELD)));
            }

            @Override
            public <T> DataResult<AltarRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
                return base.decode(ops, input).map(recipe -> {
                    T value = input.get(FIELD);

                    if (value != null) {
                        ops.getStringValue(value).result().map(ResourceLocation::tryParse).ifPresent(id -> REQUIRED.put(recipe, id));
                    }

                    return recipe;
                });
            }

            @Override
            public <T> RecordBuilder<T> encode(AltarRecipe input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                RecordBuilder<T> builder = base.encode(input, ops, prefix);
                ResourceLocation required = REQUIRED.get(input);

                if (required != null) {
                    builder.add(FIELD, ops.createString(required.toString()));
                }

                return builder;
            }

            @Override
            public String toString() {
                return base + "+" + FIELD;
            }
        };
    }

    public static StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> wrap(StreamCodec<RegistryFriendlyByteBuf, AltarRecipe> base) {
        return StreamCodec.of((buf, recipe) -> {
            base.encode(buf, recipe);
            ResourceLocation required = REQUIRED.get(recipe);
            buf.writeUtf(required == null ? "" : required.toString());
        }, buf -> {
            AltarRecipe recipe = base.decode(buf);
            String required = buf.readUtf();

            if (!required.isEmpty()) {
                REQUIRED.put(recipe, ResourceLocation.parse(required));
            }

            return recipe;
        });
    }
}
