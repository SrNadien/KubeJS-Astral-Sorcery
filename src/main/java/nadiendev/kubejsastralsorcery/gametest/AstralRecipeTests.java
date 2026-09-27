package nadiendev.kubejsastralsorcery.gametest;

import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.recipe.altar.AltarRecipe;
import hellfirepvp.astralsorcery.common.recipe.focal.drop.FocalCombineRecipe;
import hellfirepvp.astralsorcery.common.research.ResearchNode;
import hellfirepvp.astralsorcery.common.research.data.ResearchNodeLoader;
import hellfirepvp.astralsorcery.common.research.tome.TomePage;
import hellfirepvp.astralsorcery.common.research.tome.TomePageRecipe;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@GameTestHolder(AstralSorceryKJS.MOD_ID)
@PrefixGameTestTemplate(false)
public class AstralRecipeTests {
    private static final Map<String, String> EXPECTED = Map.ofEntries(
        Map.entry("kubejs:test/altar_basic", "astralsorcery:altar_crafting"),
        Map.entry("kubejs:test/altar_full", "astralsorcery:altar_crafting"),
        Map.entry("kubejs:test/altar_mapping_fluid_key", "astralsorcery:altar_crafting"),
        Map.entry("kubejs:test/altar_set_block", "astralsorcery:altar_crafting"),
        Map.entry("kubejs:test/combine", "astralsorcery:focal_combine"),
        Map.entry("kubejs:test/combine_multi", "astralsorcery:focal_combine"),
        Map.entry("kubejs:test/transmutation", "astralsorcery:focal_transmutation"),
        Map.entry("kubejs:test/transmutation_weighted", "astralsorcery:focal_transmutation"),
        Map.entry("kubejs:test/lumen", "astralsorcery:lumen_generation"),
        Map.entry("kubejs:test/lumen_combo", "astralsorcery:lumen_generation"),
        Map.entry("kubejs:test/crystallization", "astralsorcery:lumen_crystallization"),
        Map.entry("kubejs:test/lightwell", "astralsorcery:lightwell"),
        Map.entry("kubejs:test/infusion", "astralsorcery:infusion"),
        Map.entry("kubejs:test/infusion_water", "astralsorcery:infusion"),
        Map.entry("kubejs:test/liquid_starlight_drop", "astralsorcery:liquid_starlight"),
        Map.entry("kubejs:test/liquid_starlight_bind", "astralsorcery:liquid_starlight"),
        Map.entry("kubejs:test/interaction", "astralsorcery:liquid_interaction"),
        Map.entry("kubejs:test/interaction_entity", "astralsorcery:liquid_interaction")
    );

    private static final List<String> REMOVED = List.of(
        "astralsorcery:altar/illumination_wand",
        "astralsorcery:altar/dynamism_gem_sky",
        "astralsorcery:infusion/glass_lens",
        "astralsorcery:infusion/resonating_gem",
        "astralsorcery:altar/altar_radiance",
        "astralsorcery:altar/altar_luminance",
        "astralsorcery:focal_transmutation/cake"
    );

    @GameTest(template = "platform")
    public static void scriptRecipesLoaded(GameTestHelper helper) {
        RecipeManager manager = helper.getLevel().getRecipeManager();
        List<String> problems = new ArrayList<>();

        EXPECTED.forEach((id, type) -> {
            Optional<RecipeHolder<?>> holder = manager.byKey(ResourceLocation.parse(id));

            if (holder.isEmpty()) {
                problems.add("missing " + id);
            } else {
                ResourceLocation actual = BuiltInRegistries.RECIPE_TYPE.getKey(holder.get().value().getType());

                if (!type.equals(String.valueOf(actual))) {
                    problems.add(id + " has type " + actual);
                }
            }
        });

        for (String id : REMOVED) {
            if (manager.byKey(ResourceLocation.parse(id)).isPresent()) {
                problems.add("not removed " + id);
            }
        }

        if (!problems.isEmpty()) {
            helper.fail(String.join(", ", problems));
        }

        helper.succeed();
    }

    @GameTest(template = "platform")
    public static void inputsReplaced(GameTestHelper helper) {
        RecipeManager manager = helper.getLevel().getRecipeManager();
        FocalCombineRecipe wand = (FocalCombineRecipe) manager.byKey(ResourceLocation.parse("astralsorcery:focal_combine/wand")).orElseThrow().value();
        helper.assertTrue(wand.getInputs().stream().anyMatch(in -> in.test(new ItemStack(Items.EMERALD))), "wand combine still has no emerald");
        helper.assertTrue(wand.getInputs().stream().noneMatch(in -> in.test(new ItemStack(ItemsAS.AQUAMARINE.get()))), "wand combine still has aquamarine");

        AltarRecipe akashic = (AltarRecipe) manager.byKey(ResourceLocation.parse("astralsorcery:altar/akashic_singularity_with_generated_artifact_loot")).orElseThrow().value();
        helper.assertTrue(akashic.getGrid().getInputs().stream().anyMatch(in -> !in.isEmpty() && in.test(new ItemStack(Items.GLOWSTONE_DUST))), "akashic grid has no glowstone dust");
        helper.succeed();
    }

    @GameTest(template = "platform")
    public static void researchInjected(GameTestHelper helper) {
        ResearchNodeLoader loader = ResearchNodeLoader.getInstance();
        Optional<ResearchNode> created = loader.getNode(ResourceLocation.parse("kubejs:starry_diamond"));
        helper.assertTrue(created.isPresent(), "kubejs:starry_diamond was not injected");
        helper.assertTrue(created.get().getName().getString().equals("Starry Diamond"), "title was " + created.get().getName().getString());
        helper.assertTrue(created.get().getPages().size() == 5, "starry_diamond has " + created.get().getPages().size() + " pages");

        TomePage recipePage = created.get().getPages().get(1);
        helper.assertTrue(recipePage instanceof TomePageRecipe r && r.type().location().toString().equals("astralsorcery:altar_crafting"), "page 1 is not an altar recipe page: " + recipePage);
        helper.assertTrue(loader.getNode(ResourceLocation.parse("astralsorcery:chisel")).isEmpty(), "astralsorcery:chisel was not removed");

        Optional<ResearchNode> welcome = loader.getNode(ResourceLocation.parse("astralsorcery:welcome"));
        helper.assertTrue(welcome.isPresent(), "welcome node vanished");
        helper.succeed();
    }
}
