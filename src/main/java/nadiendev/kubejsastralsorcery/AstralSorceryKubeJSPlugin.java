package nadiendev.kubejsastralsorcery;

import com.google.gson.JsonElement;
import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.plugin.builtin.event.ServerEvents;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeMappingRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import nadiendev.kubejsastralsorcery.altar.AltarBlockBuilder;
import nadiendev.kubejsastralsorcery.altar.RealAltarTiers;
import nadiendev.kubejsastralsorcery.constellation.ConstellationBuilder;
import nadiendev.kubejsastralsorcery.constellation.ConstellationData;
import nadiendev.kubejsastralsorcery.recipe.AstralRecipesKubeEvent;
import nadiendev.kubejsastralsorcery.recipe.component.AstralComponents;
import nadiendev.kubejsastralsorcery.recipe.schema.AstralSchemas;
import nadiendev.kubejsastralsorcery.research.ResearchInjector;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Map;

public class AstralSorceryKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(AstralSorceryEvents.GROUP);
    }

    @Override
    public void afterScriptsLoaded(ScriptManager manager) {
        if (manager.scriptType == ScriptType.SERVER && AstralSorceryEvents.RECIPES.hasListeners()) {
            ServerEvents.RECIPES.listenJava(ScriptType.SERVER, null, event -> {
                AstralSorceryEvents.RECIPES.post(ScriptType.SERVER, new AstralRecipesKubeEvent((RecipesKubeEvent) event));
                return null;
            });
        }
    }

    @Override
    public void beforeRecipeLoading(RecipesKubeEvent event, RecipeManagerKJS manager, Map<ResourceLocation, JsonElement> recipeJsons) {
        ResearchInjector.setLatestRecipes((RecipeManager) manager);
    }

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.addDefault(RegistriesAS.KEY_CONSTELLATIONS, ConstellationBuilder.class, ConstellationBuilder::new);
        registry.of(Registries.BLOCK, reg -> reg.add(AstralSorceryKJS.as("altar"), AltarBlockBuilder.class, AltarBlockBuilder::new));
    }

    @Override
    public void initStartup() {
        RealAltarTiers.ensureDefinitionsLoaded();
    }

    @Override
    public void generateData(KubeDataGenerator generator) {
        ConstellationData.generateData(generator);
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        ConstellationData.generateAssets(generator);
    }

    @Override
    public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
        AstralComponents.register(registry);
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        RecipeNamespace namespace = registry.namespace(AstralSorceryKJS.AS_ID);
        namespace.register("altar_crafting", AstralSchemas.ALTAR);
        namespace.register("focal_combine", AstralSchemas.FOCAL_COMBINE);
        namespace.register("focal_transmutation", AstralSchemas.FOCAL_TRANSMUTATION);
        namespace.register("lumen_generation", AstralSchemas.LUMEN_GENERATION);
        namespace.register("lumen_crystallization", AstralSchemas.LUMEN_CRYSTALLIZATION);
        namespace.register("lightwell", AstralSchemas.LIGHTWELL);
        namespace.register("infusion", AstralSchemas.INFUSION);
        namespace.register("liquid_starlight", AstralSchemas.LIQUID_STARLIGHT);
        namespace.register("liquid_interaction", AstralSchemas.LIQUID_INTERACTION);
        namespace.register("illumination_wand_change_color", AstralSchemas.SPECIAL);
        namespace.register("celestial_gateway_change_color", AstralSchemas.SPECIAL);
    }

    @Override
    public void registerRecipeMappings(RecipeMappingRegistry registry) {
        registry.register("astralAltar", AstralSorceryKJS.as("altar_crafting"));
        registry.register("astralCombine", AstralSorceryKJS.as("focal_combine"));
        registry.register("astralTransmutation", AstralSorceryKJS.as("focal_transmutation"));
        registry.register("astralLumen", AstralSorceryKJS.as("lumen_generation"));
        registry.register("astralCrystallization", AstralSorceryKJS.as("lumen_crystallization"));
        registry.register("astralLightwell", AstralSorceryKJS.as("lightwell"));
        registry.register("astralInfusion", AstralSorceryKJS.as("infusion"));
        registry.register("astralLiquidStarlight", AstralSorceryKJS.as("liquid_starlight"));
        registry.register("astralLiquidInteraction", AstralSorceryKJS.as("liquid_interaction"));
    }
}
