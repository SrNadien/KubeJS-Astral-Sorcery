package nadiendev.kubejsastralsorcery.recipe.schema;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.FluidStackComponent;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.ListRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.MapRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.IntBounds;
import dev.latvian.mods.kubejs.util.TinyMap;
import hellfirepvp.astralsorcery.common.constellation.BaseConstellation;
import hellfirepvp.astralsorcery.common.ingredient.IngredientBridge;
import hellfirepvp.astralsorcery.common.lumen.Lumen;
import hellfirepvp.astralsorcery.common.lumen.LumenStack;
import hellfirepvp.astralsorcery.common.recipe.altar.effect.AltarEffect;
import hellfirepvp.astralsorcery.common.recipe.altar.output.AltarRecipeOutputModifier;
import hellfirepvp.astralsorcery.common.recipe.liquid.interaction.result.LiquidInteractionResult;
import hellfirepvp.astralsorcery.common.recipe.liquid.output.LiquidStarlightRecipeOutputModifier;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import nadiendev.kubejsastralsorcery.recipe.AstralFormat;
import nadiendev.kubejsastralsorcery.recipe.component.AstralComponents;
import nadiendev.kubejsastralsorcery.recipe.component.NestedComponent;
import nadiendev.kubejsastralsorcery.recipe.kube.AltarKubeRecipe;
import nadiendev.kubejsastralsorcery.recipe.kube.FocalCombineKubeRecipe;
import nadiendev.kubejsastralsorcery.recipe.kube.FocalTransmutationKubeRecipe;
import nadiendev.kubejsastralsorcery.recipe.kube.LiquidInteractionKubeRecipe;
import nadiendev.kubejsastralsorcery.recipe.kube.LiquidStarlightKubeRecipe;
import nadiendev.kubejsastralsorcery.recipe.kube.LumenGenerationKubeRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface AstralSchemas {
    RecipeComponent<ItemStack> ITEM = ItemStackComponent.ITEM_STACK.instance();
    RecipeComponent<Ingredient> INGREDIENT = IngredientComponent.INGREDIENT.instance();
    RecipeComponent<String> STRING = StringComponent.STRING.instance();
    RecipeComponent<BaseConstellation> CONSTELLATION = AstralComponents.of(AstralComponents.CONSTELLATION);
    RecipeComponent<Lumen> LUMEN = AstralComponents.of(AstralComponents.LUMEN);
    RecipeComponent<Integer> COLOR = AstralComponents.of(AstralComponents.COLOR);
    RecipeComponent<Fluid> FLUID = AstralComponents.of(AstralComponents.FLUID);

    List<String> EMPTY_RELAY = List.of("     ", "     ", "     ", "     ", "     ");

    static KubeRecipeFactory factory(String name, Class<?> type, Supplier<? extends KubeRecipe> supplier) {
        return new KubeRecipeFactory(AstralSorceryKJS.id(name), type, supplier);
    }

    static String k(String camel) {
        return AstralFormat.key(camel);
    }

    static String o(String camel) {
        return AstralFormat.other(AstralFormat.key(camel));
    }

    static <T> ListRecipeComponent<T> list(RecipeComponent<T> component) {
        return ListRecipeComponent.create(component, false, false, IntBounds.OPTIONAL, Optional.empty());
    }

    static RecipeKey<Integer> duration(int def) {
        return NumberComponent.INT.otherKey("duration").optional(def).alwaysWrite();
    }

    static RecipeKey<Integer> color(String name) {
        return COLOR.otherKey(name).optional(-1).alwaysWrite();
    }

    RecipeKey<List<ItemStack>> ALTAR_OUTPUTS = ITEM.asList().outputKey("outputs");
    RecipeKey<List<String>> ALTAR_PATTERN = new NestedComponent<>(STRING.asList(), "grid").otherKey("pattern");
    RecipeKey<TinyMap<Character, IngredientBridge>> ALTAR_KEY = new NestedComponent<>(AstralComponents.of(AstralComponents.INGREDIENT_BRIDGE).asPatternKey(), "grid").inputKey("key");
    RecipeKey<List<String>> ALTAR_RELAY = new NestedComponent<>(STRING.asList(), "grid").otherKey(k("relayPattern")).alt(o("relayPattern")).optional(EMPTY_RELAY).alwaysWrite().functionNames("relay", "relayPattern");
    RecipeKey<TileAltar.AltarType> ALTAR_TYPE = AstralComponents.of(AstralComponents.ALTAR_TYPE).otherKey(k("requiredType")).alt(o("requiredType")).optional(TileAltar.AltarType.ILLUMINATION).alwaysWrite().functionNames("requiredType", "tier", "altarTier");
    RecipeKey<BaseConstellation> ALTAR_FOCUS = CONSTELLATION.otherKey(k("focusConstellation")).alt(o("focusConstellation")).defaultOptional().functionNames("constellation", "focus", "focusConstellation");
    RecipeKey<Float> ALTAR_SHATTER = NumberComponent.FLOAT.otherKey(k("baseFocusShatterChance")).alt(o("baseFocusShatterChance")).optional(0F).alwaysWrite().functionNames("shatterChance", "focusShatterChance", "baseFocusShatterChance");
    RecipeKey<Integer> ALTAR_DURATION = duration(100);
    RecipeKey<Boolean> ALTAR_ONLY_NIGHT = BooleanComponent.BOOLEAN.instance().otherKey(k("onlyNight")).alt(o("onlyNight")).optional(true).alwaysWrite();
    RecipeKey<Boolean> ALTAR_MAY_CHAIN = BooleanComponent.BOOLEAN.instance().otherKey(k("mayChain")).alt(o("mayChain")).optional(false).alwaysWrite();
    RecipeKey<List<BaseConstellation>> ALTAR_STARLIGHT = list(CONSTELLATION).otherKey(k("requiredStarlight")).alt(o("requiredStarlight")).optional(List.of()).alwaysWrite().functionNames("requiredStarlight");
    RecipeKey<List<LumenStack>> ALTAR_LUMEN = list(AstralComponents.of(AstralComponents.LUMEN_STACK)).otherKey(k("requiredLumen")).alt(o("requiredLumen")).optional(List.of()).alwaysWrite().functionNames("requiredLumen");
    RecipeKey<List<FluidStack>> ALTAR_FLUIDS = list(FluidStackComponent.FLUID_STACK.instance()).inputKey(k("requiredFluid")).alt(o("requiredFluid")).optional(List.of()).alwaysWrite().functionNames("requiredFluid");
    RecipeKey<List<SizedIngredient>> ALTAR_ADDITIONAL = list(AstralComponents.of(AstralComponents.SIZED_INGREDIENT)).inputKey(k("requiredAdditionalInputs")).alt(o("requiredAdditionalInputs")).optional(List.of()).alwaysWrite().functionNames("requiredAdditionalInputs", "additionalInputs");
    RecipeKey<List<AltarEffect>> ALTAR_EFFECTS = list(AstralComponents.of(AstralComponents.ALTAR_EFFECT)).otherKey("effects").optional(List.of()).alwaysWrite().functionNames("effects");
    RecipeKey<List<AltarRecipeOutputModifier>> ALTAR_MODIFIERS = list(AstralComponents.of(AstralComponents.ALTAR_OUTPUT_MODIFIER)).otherKey(k("outputModifiers")).alt(o("outputModifiers")).optional(List.of()).alwaysWrite().functionNames("outputModifiers", "modifiers");
    RecipeKey<String> ALTAR_BLOCK = StringComponent.ID.instance().otherKey(AstralSorceryKJS.MOD_ID + ":altar").defaultOptional().functionNames("altar", "requiredAltar");

    RecipeSchema ALTAR = new RecipeSchema(ALTAR_OUTPUTS, ALTAR_PATTERN, ALTAR_KEY, ALTAR_RELAY, ALTAR_TYPE, ALTAR_FOCUS, ALTAR_SHATTER, ALTAR_DURATION, ALTAR_ONLY_NIGHT, ALTAR_MAY_CHAIN,
        ALTAR_STARLIGHT, ALTAR_LUMEN, ALTAR_FLUIDS, ALTAR_ADDITIONAL, ALTAR_EFFECTS, ALTAR_MODIFIERS, ALTAR_BLOCK)
        .factory(factory("altar", AltarKubeRecipe.class, AltarKubeRecipe::new))
        .constructor(ALTAR_OUTPUTS, ALTAR_PATTERN, ALTAR_KEY)
        .uniqueId(ALTAR_OUTPUTS);

    RecipeKey<List<ItemStack>> COMBINE_OUTPUTS = ITEM.asList().outputKey("outputs");
    RecipeKey<List<Ingredient>> COMBINE_INPUTS = INGREDIENT.asList().inputKey("inputs");
    RecipeKey<Integer> COMBINE_DURATION = duration(100);
    RecipeKey<Integer> COMBINE_COLOR = color("color");
    RecipeKey<BaseConstellation> COMBINE_CONSTELLATION = CONSTELLATION.otherKey("required_constellation").defaultOptional().functionNames("constellation", "requiredConstellation");

    RecipeSchema FOCAL_COMBINE = new RecipeSchema(COMBINE_OUTPUTS, COMBINE_INPUTS, COMBINE_DURATION, COMBINE_COLOR, COMBINE_CONSTELLATION)
        .factory(factory("focal_combine", FocalCombineKubeRecipe.class, FocalCombineKubeRecipe::new))
        .constructor(COMBINE_OUTPUTS, COMBINE_INPUTS)
        .uniqueId(COMBINE_OUTPUTS);

    RecipeKey<List<WeightedEntry.Wrapper<BlockState>>> TRANSMUTATION_OUTPUTS = AstralComponents.of(AstralComponents.WEIGHTED_BLOCK_STATE).asList().otherKey("output_states");
    RecipeKey<List<BlockPredicate>> TRANSMUTATION_INPUTS = AstralComponents.of(AstralComponents.BLOCK_PREDICATE).asList().otherKey("input_predicates");
    RecipeKey<Ingredient> TRANSMUTATION_DISPLAY = IngredientComponent.OPTIONAL_INGREDIENT.instance().inputKey("input_display_stacks").optional(Ingredient.EMPTY).alwaysWrite().functionNames("display", "displayItems", "inputDisplayStacks");
    RecipeKey<Integer> TRANSMUTATION_DURATION = duration(100);
    RecipeKey<Integer> TRANSMUTATION_COLOR = color("color");
    RecipeKey<BaseConstellation> TRANSMUTATION_CONSTELLATION = CONSTELLATION.otherKey("required_constellation").defaultOptional().functionNames("constellation", "requiredConstellation");
    RecipeKey<Boolean> TRANSMUTATION_FOCUSED = BooleanComponent.BOOLEAN.instance().otherKey("requires_focused_starlight").optional(false).alwaysWrite().functionNames("focused", "requiresFocusedStarlight");

    RecipeSchema FOCAL_TRANSMUTATION = new RecipeSchema(TRANSMUTATION_OUTPUTS, TRANSMUTATION_INPUTS, TRANSMUTATION_DISPLAY, TRANSMUTATION_DURATION, TRANSMUTATION_COLOR, TRANSMUTATION_CONSTELLATION, TRANSMUTATION_FOCUSED)
        .factory(factory("focal_transmutation", FocalTransmutationKubeRecipe.class, FocalTransmutationKubeRecipe::new))
        .constructor(TRANSMUTATION_OUTPUTS, TRANSMUTATION_INPUTS);

    RecipeKey<Lumen> GENERATION_LUMEN = LUMEN.otherKey("produced_lumen");
    RecipeKey<Ingredient> GENERATION_INPUT = INGREDIENT.inputKey("input");
    RecipeKey<Integer> GENERATION_AMOUNT = NumberComponent.INT.otherKey("produced_lumen_amount").optional(1).alwaysWrite().functionNames("amount", "producedAmount", "producedLumenAmount");
    RecipeKey<Float> GENERATION_ATTEMPTS = NumberComponent.FLOAT.otherKey("production_attempt_multiplier").optional(1F).alwaysWrite().functionNames("attemptMultiplier", "productionAttemptMultiplier");
    RecipeKey<Float> GENERATION_STARLIGHT = NumberComponent.FLOAT.otherKey("attempt_starlight_consumption").optional(1F).alwaysWrite().functionNames("starlightConsumption", "attemptStarlightConsumption");
    RecipeKey<Float> GENERATION_SHATTER = NumberComponent.FLOAT.otherKey("catalyst_shatter_multiplier").optional(1F).alwaysWrite().functionNames("shatterMultiplier", "catalystShatterMultiplier");
    RecipeKey<TinyMap<Lumen, Integer>> GENERATION_COMBINATION = MapRecipeComponent.of(LUMEN, NumberComponent.INT, IntBounds.OPTIONAL).otherKey("lumen_combination_inputs").optional(new TinyMap<>(List.of())).alwaysWrite().functionNames("combination", "lumenCombinationInputs");

    RecipeSchema LUMEN_GENERATION = new RecipeSchema(GENERATION_LUMEN, GENERATION_INPUT, GENERATION_AMOUNT, GENERATION_ATTEMPTS, GENERATION_STARLIGHT, GENERATION_SHATTER, GENERATION_COMBINATION)
        .factory(factory("lumen_generation", LumenGenerationKubeRecipe.class, LumenGenerationKubeRecipe::new))
        .constructor(GENERATION_LUMEN, GENERATION_INPUT)
        .uniqueId(GENERATION_LUMEN);

    RecipeKey<Lumen> CRYSTALLIZATION_LUMEN = LUMEN.otherKey("lumen_to_crystallize");
    RecipeKey<Ingredient> CRYSTALLIZATION_INPUT = INGREDIENT.inputKey("input");
    RecipeKey<Float> CRYSTALLIZATION_SHATTER = NumberComponent.FLOAT.otherKey("catalyst_shatter_multiplier").optional(1F).alwaysWrite().functionNames("shatterMultiplier", "catalystShatterMultiplier");
    RecipeKey<Integer> CRYSTALLIZATION_CONSUMED = NumberComponent.intRange(1, 1900).otherKey("lumen_consumed_per_operation").optional(1400).alwaysWrite().functionNames("lumenPerOperation", "lumenConsumedPerOperation");

    RecipeSchema LUMEN_CRYSTALLIZATION = new RecipeSchema(CRYSTALLIZATION_LUMEN, CRYSTALLIZATION_INPUT, CRYSTALLIZATION_SHATTER, CRYSTALLIZATION_CONSUMED)
        .constructor(CRYSTALLIZATION_LUMEN, CRYSTALLIZATION_INPUT)
        .uniqueId(CRYSTALLIZATION_LUMEN);

    RecipeKey<Fluid> LIGHTWELL_FLUID = FLUID.otherKey("generated_fluid");
    RecipeKey<Ingredient> LIGHTWELL_INPUT = INGREDIENT.inputKey("input");
    RecipeKey<Integer> LIGHTWELL_COLOR = color("catalyst_color").functionNames("color", "catalystColor");
    RecipeKey<Float> LIGHTWELL_PRODUCTION = NumberComponent.FLOAT.otherKey("production_multiplier").optional(0.5F).alwaysWrite().functionNames("productionMultiplier", "production");
    RecipeKey<Float> LIGHTWELL_SHATTER = NumberComponent.FLOAT.otherKey("shatter_multiplier").optional(10F).alwaysWrite().functionNames("shatterMultiplier");

    RecipeSchema LIGHTWELL = new RecipeSchema(LIGHTWELL_FLUID, LIGHTWELL_INPUT, LIGHTWELL_COLOR, LIGHTWELL_PRODUCTION, LIGHTWELL_SHATTER)
        .constructor(LIGHTWELL_FLUID, LIGHTWELL_INPUT)
        .uniqueId(LIGHTWELL_INPUT);

    RecipeKey<ItemStack> INFUSION_OUTPUT = ITEM.outputKey("output");
    RecipeKey<Ingredient> INFUSION_INPUT = INGREDIENT.inputKey("item_input");
    RecipeKey<Fluid> INFUSION_FLUID = FLUID.otherKey("fluid_input").optional(type -> BuiltInRegistries.FLUID.get(AstralSorceryKJS.as("liquid_starlight"))).alwaysWrite().functionNames("fluid", "fluidInput");
    RecipeKey<Integer> INFUSION_DURATION = duration(200);
    RecipeKey<Float> INFUSION_CHANCE = NumberComponent.FLOAT.otherKey("fluid_consumption_chance").optional(0.05F).alwaysWrite().functionNames("consumptionChance", "fluidConsumptionChance");
    RecipeKey<Boolean> INFUSION_MULTIPLE = BooleanComponent.BOOLEAN.instance().otherKey("consume_multiple_fluids").optional(false).alwaysWrite().functionNames("consumeMultipleFluids", "multipleFluids");
    RecipeKey<Boolean> INFUSION_CHALICE = BooleanComponent.BOOLEAN.instance().otherKey("accept_chalice_input").optional(true).alwaysWrite().functionNames("chalice", "acceptChaliceInput");

    RecipeSchema INFUSION = new RecipeSchema(INFUSION_OUTPUT, INFUSION_INPUT, INFUSION_FLUID, INFUSION_DURATION, INFUSION_CHANCE, INFUSION_MULTIPLE, INFUSION_CHALICE)
        .constructor(INFUSION_OUTPUT, INFUSION_INPUT)
        .uniqueId(INFUSION_OUTPUT);

    RecipeKey<List<LiquidStarlightRecipeOutputModifier>> STARLIGHT_OUTPUTS = list(AstralComponents.of(AstralComponents.LIQUID_STARLIGHT_OUTPUT)).outputKey(k("outputModifiers")).alt(o("outputModifiers")).functionNames("outputModifiers", "modifiers");
    RecipeKey<SizedIngredient> STARLIGHT_INPUT = AstralComponents.of(AstralComponents.SIZED_INGREDIENT).inputKey("input");
    RecipeKey<List<SizedIngredient>> STARLIGHT_OTHER = list(AstralComponents.of(AstralComponents.SIZED_INGREDIENT)).inputKey(k("otherInputs")).alt(o("otherInputs")).optional(List.of()).alwaysWrite().functionNames("otherInputs", "extraInputs");
    RecipeKey<Integer> STARLIGHT_DURATION = duration(60);
    RecipeKey<Integer> STARLIGHT_RANDOM = NumberComponent.INT.otherKey(k("randomAdditionalDuration")).alt(o("randomAdditionalDuration")).optional(20).alwaysWrite().functionNames("randomDuration", "randomAdditionalDuration");
    RecipeKey<Integer> STARLIGHT_COLOR = color("color");
    RecipeKey<Boolean> STARLIGHT_CONSUMES_LIQUID = BooleanComponent.BOOLEAN.instance().otherKey(k("consumesLiquid")).alt(o("consumesLiquid")).optional(false).alwaysWrite();
    RecipeKey<Boolean> STARLIGHT_CONSUMES_INPUTS = BooleanComponent.BOOLEAN.instance().otherKey(k("consumesInputs")).alt(o("consumesInputs")).optional(true).alwaysWrite();

    RecipeSchema LIQUID_STARLIGHT = new RecipeSchema(STARLIGHT_OUTPUTS, STARLIGHT_INPUT, STARLIGHT_OTHER, STARLIGHT_DURATION, STARLIGHT_RANDOM, STARLIGHT_COLOR, STARLIGHT_CONSUMES_LIQUID, STARLIGHT_CONSUMES_INPUTS)
        .factory(factory("liquid_starlight", LiquidStarlightKubeRecipe.class, LiquidStarlightKubeRecipe::new))
        .constructor(STARLIGHT_OUTPUTS, STARLIGHT_INPUT);

    RecipeKey<LiquidInteractionResult> INTERACTION_RESULT = AstralComponents.of(AstralComponents.LIQUID_INTERACTION_RESULT).outputKey("result");
    RecipeKey<SizedFluidIngredient> INTERACTION_A = SizedFluidIngredientComponent.NESTED.instance().inputKey(k("reactantA")).alt(o("reactantA"));
    RecipeKey<SizedFluidIngredient> INTERACTION_B = SizedFluidIngredientComponent.NESTED.instance().inputKey(k("reactantB")).alt(o("reactantB"));
    RecipeKey<Float> INTERACTION_CHANCE_A = NumberComponent.FLOAT.otherKey(k("chanceConsumeA")).alt(o("chanceConsumeA")).optional(1F).alwaysWrite().functionNames("chanceA", "chanceConsumeA");
    RecipeKey<Float> INTERACTION_CHANCE_B = NumberComponent.FLOAT.otherKey(k("chanceConsumeB")).alt(o("chanceConsumeB")).optional(1F).alwaysWrite().functionNames("chanceB", "chanceConsumeB");
    RecipeKey<Integer> INTERACTION_WEIGHT = NumberComponent.INT.otherKey("weight").optional(1).alwaysWrite();

    RecipeSchema LIQUID_INTERACTION = new RecipeSchema(INTERACTION_RESULT, INTERACTION_A, INTERACTION_B, INTERACTION_CHANCE_A, INTERACTION_CHANCE_B, INTERACTION_WEIGHT)
        .factory(factory("liquid_interaction", LiquidInteractionKubeRecipe.class, LiquidInteractionKubeRecipe::new))
        .constructor(INTERACTION_RESULT, INTERACTION_A, INTERACTION_B);

    RecipeKey<String> SPECIAL_CATEGORY = StringComponent.STRING.instance().otherKey("category").optional("misc").alwaysWrite();

    RecipeSchema SPECIAL = new RecipeSchema(SPECIAL_CATEGORY)
        .constructor();
}
