package nadiendev.kubejsastralsorcery.gametest;

import hellfirepvp.astralsorcery.common.config.server.GeneralConfig;
import hellfirepvp.astralsorcery.common.config.server.PerkConfig;
import com.mojang.authlib.GameProfile;
import hellfirepvp.astralsorcery.common.integration.IntegrationCurios;
import hellfirepvp.astralsorcery.common.lumen.binding.LumenBindingType;
import hellfirepvp.astralsorcery.common.lumen.binding.data.LumenBindingTypeLoader;
import hellfirepvp.astralsorcery.common.tile.TileTreeBeacon;
import hellfirepvp.astralsorcery.common.tile.network.FocusCrystalSourceNode;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import nadiendev.kubejsastralsorcery.focal.FocalPointTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.UUID;

@GameTestHolder(AstralSorceryKJS.MOD_ID)
@PrefixGameTestTemplate(false)
public class AstralTweakTests {
    @GameTest(template = "platform")
    public static void lumenBindingsModified(GameTestHelper helper) {
        LumenBindingTypeLoader loader = LumenBindingTypeLoader.getInstance();
        LumenBindingType aevitas = loader.getBindingType(LogicalSide.SERVER, AstralSorceryKJS.as("aevitas")).orElse(null);
        helper.assertTrue(aevitas != null, "aevitas binding missing");

        for (int i = 0; i < 20; i++) {
            MobEffectInstance effect = aevitas.getPotionEffect().orElseThrow();
            helper.assertTrue(effect.getDuration() >= 100 && effect.getDuration() <= 200, "aevitas potion lasts " + effect.getDuration());
        }

        int cost = aevitas.getBinding(LumenBindingType.SlotType.HELMET).orElseThrow().getLumenUsage().getLumenCost();
        helper.assertTrue(cost == 5, "aevitas helmet cost is " + cost);

        Optional<LumenBindingType> custom = loader.getBindingType(LogicalSide.SERVER, ResourceLocation.parse("kubejs:test_binding"));
        helper.assertTrue(custom.isPresent(), "kubejs:test_binding was not created");
        helper.assertTrue(custom.get().getBinding(LumenBindingType.SlotType.TOOL).isPresent(), "test binding has no tool slot");

        Optional<ResourceLocation> armara = loader.getLumenBindingType(LogicalSide.SERVER, AstralSorceryKJS.as("armara"));
        helper.assertTrue(armara.isPresent() && armara.get().toString().equals("kubejs:test_binding"), "armara maps to " + armara);
        helper.assertTrue(loader.getBindingType(LogicalSide.SERVER, AstralSorceryKJS.as("discidia")).isEmpty(), "discidia binding was not removed");
        helper.succeed();
    }

    @GameTest(template = "platform")
    public static void configValuesSet(GameTestHelper helper) {
        helper.assertTrue(GeneralConfig.CONFIG.dayLength.get() == 30000, "dayLength is " + GeneralConfig.CONFIG.dayLength.get());
        helper.assertTrue(PerkConfig.CONFIG.perkLevelCap.get() == 50, "perkLevelCap is " + PerkConfig.CONFIG.perkLevelCap.get());
        helper.assertTrue(TileTreeBeacon.CONFIG.range.get() == 20D, "tree beacon range is " + TileTreeBeacon.CONFIG.range.get());
        helper.succeed();
    }

    @GameTest(template = "platform")
    public static void curiosMissingDoesNotCrash(GameTestHelper helper) {
        helper.assertTrue(!ModList.get().isLoaded("curios"), "this test needs a run without Curios");
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "curios_test"));
        helper.assertTrue(IntegrationCurios.getCurio(player, stack -> true).isEmpty(), "curio lookup returned items");
        helper.succeed();
    }

    @GameTest(template = "platform")
    public static void focalPointTweaked(GameTestHelper helper) {
        helper.assertTrue(FocalPointTweaks.flightSize == 25F, "flight box is " + FocalPointTweaks.flightSize);
        helper.assertTrue(FocalPointTweaks.flightDuration == 200, "flight duration is " + FocalPointTweaks.flightDuration);
        helper.assertTrue(FocalPointTweaks.starlightMultiplier == 2F, "starlight multiplier is " + FocalPointTweaks.starlightMultiplier);
        Optional<Integer> distance = new FocusCrystalSourceNode(BlockPos.ZERO).getMaxBlockLinkDistance();
        helper.assertTrue(distance.isPresent() && distance.get() == 24, "link distance is " + distance);
        helper.succeed();
    }
}
