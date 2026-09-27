package nadiendev.kubejsastralsorcery.recipe.component;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.BlockWrapper;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import hellfirepvp.astralsorcery.common.constellation.BaseConstellation;
import hellfirepvp.astralsorcery.common.ingredient.IngredientBridge;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.lumen.Lumen;
import hellfirepvp.astralsorcery.common.lumen.LumenStack;
import hellfirepvp.astralsorcery.common.recipe.altar.effect.AltarEffect;
import hellfirepvp.astralsorcery.common.recipe.altar.output.AltarRecipeOutputModifier;
import hellfirepvp.astralsorcery.common.recipe.liquid.interaction.result.LiquidInteractionResult;
import hellfirepvp.astralsorcery.common.recipe.liquid.output.LiquidStarlightRecipeOutputModifier;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import hellfirepvp.astralsorcery.common.util.data.CountIngredient;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.material.Fluid;

import java.util.Locale;

public final class AstralComponents {
    private AstralComponents() {
    }

    public static final RecipeComponentType<TileAltar.AltarType> ALTAR_TYPE = RecipeComponentType.unit(AstralSorceryKJS.id("altar_type"), type -> CodecComponent.of(type, TileAltar.AltarType.CODEC, TileAltar.AltarType.class,
        (cx, from) -> from instanceof CharSequence s ? TileAltar.AltarType.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(s.toString().trim().toLowerCase(Locale.ROOT))).getOrThrow() : null));

    public static final RecipeComponentType<BaseConstellation> CONSTELLATION = RecipeComponentType.unit(AstralSorceryKJS.id("constellation"),
        type -> new AstralRegistryComponent<>(type, RegistriesAS.REGISTRY_CONSTELLATIONS, BaseConstellation.class, AstralSorceryKJS.AS_ID));

    public static final RecipeComponentType<Lumen> LUMEN = RecipeComponentType.unit(AstralSorceryKJS.id("lumen"),
        type -> new AstralRegistryComponent<>(type, RegistriesAS.REGISTRY_LUMEN, Lumen.class, AstralSorceryKJS.AS_ID));

    public static final RecipeComponentType<AltarEffect> ALTAR_EFFECT = RecipeComponentType.unit(AstralSorceryKJS.id("altar_effect"),
        type -> new AstralRegistryComponent<>(type, RegistriesAS.REGISTRY_ALTAR_EFFECTS, AltarEffect.class, AstralSorceryKJS.AS_ID));

    public static final RecipeComponentType<Fluid> FLUID = RecipeComponentType.unit(AstralSorceryKJS.id("fluid"),
        type -> new AstralRegistryComponent<>(type, BuiltInRegistries.FLUID, Fluid.class, ResourceLocation.DEFAULT_NAMESPACE));

    public static final RecipeComponentType<Integer> COLOR = RecipeComponentType.unit(AstralSorceryKJS.id("color"), ColorComponent::new);

    public static final RecipeComponentType<IngredientBridge> INGREDIENT_BRIDGE = RecipeComponentType.unit(AstralSorceryKJS.id("ingredient_bridge"), IngredientBridgeComponent::new);

    public static final RecipeComponentType<CountIngredient> COUNT_INGREDIENT = RecipeComponentType.unit(AstralSorceryKJS.id("count_ingredient"),
        type -> new CountIngredientComponent(type, CountIngredient.CODEC_NONEMPTY, false));

    public static final RecipeComponentType<CountIngredient> OPTIONAL_COUNT_INGREDIENT = RecipeComponentType.unit(AstralSorceryKJS.id("optional_count_ingredient"),
        type -> new CountIngredientComponent(type, CountIngredient.CODEC, true));

    public static final RecipeComponentType<LumenStack> LUMEN_STACK = RecipeComponentType.unit(AstralSorceryKJS.id("lumen_stack"),
        type -> CodecComponent.of(type, LumenStack.CODEC, LumenStack.class));

    public static final RecipeComponentType<AltarRecipeOutputModifier> ALTAR_OUTPUT_MODIFIER = RecipeComponentType.unit(AstralSorceryKJS.id("altar_output_modifier"),
        type -> CodecComponent.of(type, AltarRecipeOutputModifier.CODEC, AltarRecipeOutputModifier.class));

    public static final RecipeComponentType<LiquidStarlightRecipeOutputModifier> LIQUID_STARLIGHT_OUTPUT = RecipeComponentType.unit(AstralSorceryKJS.id("liquid_starlight_output"),
        type -> new DropItemCodecComponent<>(type, LiquidStarlightRecipeOutputModifier.CODEC, LiquidStarlightRecipeOutputModifier.class, "astralsorcery:drop_item", "result"));

    public static final RecipeComponentType<LiquidInteractionResult> LIQUID_INTERACTION_RESULT = RecipeComponentType.unit(AstralSorceryKJS.id("liquid_interaction_result"),
        type -> new DropItemCodecComponent<>(type, LiquidInteractionResult.CODEC, LiquidInteractionResult.class, "astralsorcery:drop_item", "output"));

    public static final RecipeComponentType<BlockPredicate> BLOCK_PREDICATE = RecipeComponentType.unit(AstralSorceryKJS.id("block_predicate"),
        type -> CodecComponent.of(type, BlockPredicate.CODEC, BlockPredicate.class, (cx, from) -> {
            if (from instanceof Block block) {
                return BlockPredicate.matchesBlocks(block);
            }

            if (from instanceof BlockState state) {
                return BlockPredicate.matchesBlocks(state.getBlock());
            }

            if (from instanceof CharSequence s) {
                String str = s.toString().trim();

                if (str.startsWith("#")) {
                    return BlockPredicate.matchesTag(TagKey.create(Registries.BLOCK, ResourceLocation.parse(str.substring(1))));
                }

                return BlockPredicate.matchesBlocks(BlockWrapper.parseBlockState(cx.registries(), str).getBlock());
            }

            return null;
        }));

    public static final Codec<WeightedEntry.Wrapper<BlockState>> WEIGHTED_STATE_CODEC = WeightedEntry.Wrapper.codec(BlockState.CODEC);

    public static final RecipeComponentType<WeightedEntry.Wrapper<BlockState>> WEIGHTED_BLOCK_STATE = RecipeComponentType.unit(AstralSorceryKJS.id("weighted_block_state"),
        type -> CodecComponent.of(type, WEIGHTED_STATE_CODEC, WeightedEntry.Wrapper.class, (cx, from) -> {
            if (from instanceof BlockState state) {
                return WeightedEntry.wrap(state, 1);
            }

            if (from instanceof Block block) {
                return WeightedEntry.wrap(block.defaultBlockState(), 1);
            }

            if (from instanceof CharSequence s) {
                return WeightedEntry.wrap(BlockWrapper.parseBlockState(cx.registries(), s.toString().trim()), 1);
            }


            return null;
        }));

    public static void register(RecipeComponentTypeRegistry registry) {
        registry.register(ALTAR_TYPE);
        registry.register(CONSTELLATION);
        registry.register(LUMEN);
        registry.register(ALTAR_EFFECT);
        registry.register(FLUID);
        registry.register(COLOR);
        registry.register(INGREDIENT_BRIDGE);
        registry.register(COUNT_INGREDIENT);
        registry.register(OPTIONAL_COUNT_INGREDIENT);
        registry.register(LUMEN_STACK);
        registry.register(ALTAR_OUTPUT_MODIFIER);
        registry.register(LIQUID_STARLIGHT_OUTPUT);
        registry.register(LIQUID_INTERACTION_RESULT);
        registry.register(BLOCK_PREDICATE);
        registry.register(WEIGHTED_BLOCK_STATE);
    }

    @SuppressWarnings("unchecked")
    public static <T> RecipeComponent<T> of(RecipeComponentType<T> type) {
        return (RecipeComponent<T>) type.instance();
    }
}
