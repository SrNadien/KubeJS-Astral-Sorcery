package nadiendev.kubejsastralsorcery.gametest;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import hellfirepvp.astralsorcery.common.lib.TileEntitiesAS;
import hellfirepvp.astralsorcery.common.recipe.altar.AltarRecipe;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import hellfirepvp.astralsorcery.common.util.inventory.InventoryView;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import hellfirepvp.observerlib.api.util.StructureBlockArray;
import hellfirepvp.observerlib.common.change.ObserverProviderStructure;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import nadiendev.kubejsastralsorcery.altar.AltarRestrictions;
import nadiendev.kubejsastralsorcery.altar.KubeAltarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.UUID;

@GameTestHolder(AstralSorceryKJS.MOD_ID)
@PrefixGameTestTemplate(false)
public class AstralAltarTests {
    private static final ResourceLocation STELLAR = ResourceLocation.parse("kubejs:stellar_altar");
    private static final ResourceLocation CELESTIAL = ResourceLocation.parse("kubejs:celestial_altar");

    @GameTest(template = "platform")
    public static void tiersRegistered(GameTestHelper helper) {
        TileAltar.AltarType stellar = TileAltar.AltarType.valueOf("STELLAR");
        helper.assertTrue(stellar.ordinal() == 4, "STELLAR ordinal is " + stellar.ordinal());
        helper.assertTrue(TileAltar.AltarType.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("stellar")).result().orElse(null) == stellar, "codec does not know stellar");
        helper.assertTrue(stellar.isThisLater(TileAltar.AltarType.RADIANCE), "stellar is not above radiance");

        Block stellarBlock = BuiltInRegistries.BLOCK.get(STELLAR);
        Block celestialBlock = BuiltInRegistries.BLOCK.get(CELESTIAL);
        helper.assertTrue(stellarBlock instanceof KubeAltarBlock k && k.getAltarType() == stellar, "stellar altar block has the wrong type");
        helper.assertTrue(celestialBlock instanceof KubeAltarBlock k && k.getAltarType() == TileAltar.AltarType.LUMINANCE && k.isExclusive(), "celestial altar block is wrong");
        helper.assertTrue(TileEntitiesAS.ALTAR.type().isValid(stellarBlock.defaultBlockState()), "altar tile does not accept the stellar altar");
        helper.assertTrue(TileEntitiesAS.ALTAR.type().isValid(celestialBlock.defaultBlockState()), "altar tile does not accept the celestial altar");
        helper.assertTrue(RegistryProviders.getProvider(STELLAR) instanceof ObserverProviderStructure, "stellar structure missing");
        helper.assertTrue(RegistryProviders.getProvider(CELESTIAL) instanceof ObserverProviderStructure, "celestial structure missing");

        AltarRecipe stellarRecipe = (AltarRecipe) helper.getLevel().getRecipeManager().byKey(ResourceLocation.parse("kubejs:test/altar_stellar")).orElseThrow().value();
        helper.assertTrue(stellarRecipe.getRequiredType() == stellar, "stellar recipe tier is " + stellarRecipe.getRequiredType());
        AltarRecipe exclusiveRecipe = (AltarRecipe) helper.getLevel().getRecipeManager().byKey(ResourceLocation.parse("kubejs:test/altar_exclusive")).orElseThrow().value();
        helper.assertTrue(CELESTIAL.equals(AltarRestrictions.requiredAltar(exclusiveRecipe)), "exclusive recipe lost its altar: " + AltarRestrictions.requiredAltar(exclusiveRecipe));
        helper.succeed();
    }

    private static TileAltar buildAltar(GameTestHelper helper, ResourceLocation id, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        MatchableStructure structure = ((ObserverProviderStructure) RegistryProviders.getProvider(id)).getStructure();
        ((StructureBlockArray) structure).place(helper.getLevel(), pos);
        return (TileAltar) helper.getLevel().getBlockEntity(pos);
    }

    private static void fill(TileAltar altar, ItemStack... grid) {
        InventoryView inventory = altar.getTileData().getAltarInventory();

        for (int i = 0; i < grid.length; i++) {
            inventory.setStackInSlot(i, grid[i].copy());
        }

        altar.getTileData().markForUpdate();
    }

    @GameTest(template = "platform", timeoutTicks = 400)
    public static void stellarAltarCrafts(GameTestHelper helper) {
        BlockPos relative = new BlockPos(2, 8, 2);
        TileAltar altar = buildAltar(helper, STELLAR, relative);
        helper.assertTrue(altar != null, "no altar tile at the stellar altar");
        ItemStack f = new ItemStack(Items.FEATHER);
        fill(altar, f, f, f, f, new ItemStack(Items.DIAMOND), f, f, f, f);

        helper.runAfterDelay(20, () -> {
            helper.assertTrue(altar.hasStructure(), "stellar altar structure is not formed");
            Optional<RecipeHolder<AltarRecipe>> recipe = altar.findMatchingRecipe(helper.getLevel());
            helper.assertTrue(recipe.isPresent() && recipe.get().id().toString().equals("kubejs:test/altar_stellar"), "found " + recipe.map(RecipeHolder::id).orElse(null));
            altar.startCrafting(recipe.get(), UUID.randomUUID());
        });

        helper.succeedWhen(() -> {
            BlockPos abs = helper.absolutePos(relative);
            boolean crafted = !helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(abs).inflate(3), e -> e.getItem().is(Items.ELYTRA)).isEmpty();
            helper.assertTrue(crafted, "no elytra yet");
        });
    }

    @GameTest(template = "platform", timeoutTicks = 200)
    public static void exclusiveAltarRestriction(GameTestHelper helper) {
        TileAltar celestial = buildAltar(helper, CELESTIAL, new BlockPos(2, 8, 2));
        ItemStack g = new ItemStack(Items.GOLD_INGOT);
        fill(celestial, g, g, g, g, new ItemStack(Items.DIAMOND), g, g, g, g);

        BlockPos plainPos = helper.absolutePos(new BlockPos(2, 20, 2));
        helper.getLevel().setBlockAndUpdate(plainPos, BuiltInRegistries.BLOCK.get(AstralSorceryKJS.as("altar_luminance")).defaultBlockState());
        TileAltar plain = (TileAltar) helper.getLevel().getBlockEntity(plainPos);
        fill(plain, g, g, g, g, new ItemStack(Items.DIAMOND), g, g, g, g);

        helper.runAfterDelay(20, () -> {
            helper.assertTrue(celestial.hasStructure(), "celestial structure is not formed");
            Optional<RecipeHolder<AltarRecipe>> onCelestial = celestial.findMatchingRecipe(helper.getLevel());
            helper.assertTrue(onCelestial.isPresent() && onCelestial.get().id().toString().equals("kubejs:test/altar_exclusive"), "celestial found " + onCelestial.map(RecipeHolder::id).orElse(null));
            Optional<RecipeHolder<AltarRecipe>> onPlain = plain.findMatchingRecipe(helper.getLevel());
            helper.assertTrue(onPlain.isEmpty(), "plain luminance altar can craft the exclusive recipe");

            AltarRecipe normal = (AltarRecipe) helper.getLevel().getRecipeManager().byKey(ResourceLocation.parse("kubejs:test/altar_basic")).orElseThrow().value();
            helper.assertTrue(!AltarRestrictions.allows(normal, celestial), "exclusive altar accepts normal recipes");
            helper.assertTrue(AltarRestrictions.allows(normal, plain), "plain altar rejects normal recipes");
            helper.succeed();
        });
    }
}
