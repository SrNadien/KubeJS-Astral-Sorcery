package nadiendev.kubejsastralsorcery.gametest;

import hellfirepvp.astralsorcery.common.constellation.BaseConstellation;
import hellfirepvp.astralsorcery.common.constellation.property.AttunePlayerProperty;
import hellfirepvp.astralsorcery.common.constellation.property.FocusCrystalProperty;
import hellfirepvp.astralsorcery.common.constellation.property.ShowUpConditionProperty;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.lib.constants.TagsAS;
import hellfirepvp.astralsorcery.common.perk.data.PerkTree;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(AstralSorceryKJS.MOD_ID)
@PrefixGameTestTemplate(false)
public class AstralConstellationTests {
    @GameTest(template = "platform")
    public static void scriptConstellationsRegistered(GameTestHelper helper) {
        BaseConstellation lyra = RegistriesAS.REGISTRY_CONSTELLATIONS.get(ResourceLocation.parse("kubejs:lyra"));
        helper.assertTrue(lyra != null, "kubejs:lyra is not registered");
        helper.assertTrue(lyra.getStars().size() == 6, "lyra has " + lyra.getStars().size() + " stars");
        helper.assertTrue(lyra.getStarConnections().size() == 6, "lyra has " + lyra.getStarConnections().size() + " connections");
        helper.assertTrue(lyra.getTier() == BaseConstellation.Tier.MAJOR, "lyra is not major");
        helper.assertTrue(lyra.hasProperty(FocusCrystalProperty.KEY), "lyra has no focus crystal property");
        helper.assertTrue(lyra.hasProperty(AttunePlayerProperty.KEY), "lyra is not attunable");
        helper.assertTrue((lyra.getConstellationColor().getColor() & 0xFFFFFF) == 0x7F5FFF, "lyra color is " + Integer.toHexString(lyra.getConstellationColor().getColor()));
        helper.assertTrue(lyra.is(TagsAS.Constellations.MAY_BE_FOCAL_POINT), "lyra is not tagged as a focal point");

        BaseConstellation ferrum = RegistriesAS.REGISTRY_CONSTELLATIONS.get(ResourceLocation.parse("kubejs:ferrum"));
        helper.assertTrue(ferrum != null, "kubejs:ferrum is not registered");
        helper.assertTrue(ferrum.getTier() == BaseConstellation.Tier.MINOR, "ferrum is not minor");
        helper.assertTrue(ferrum.hasProperty(ShowUpConditionProperty.KEY), "ferrum has no moon phase condition");
        helper.assertTrue(!ferrum.is(TagsAS.Constellations.MAY_BE_FOCAL_POINT), "ferrum should not be a focal point");
        helper.succeed();
    }

    @GameTest(template = "platform")
    public static void scriptRootPerkLoaded(GameTestHelper helper) {
        BaseConstellation lyra = RegistriesAS.REGISTRY_CONSTELLATIONS.get(ResourceLocation.parse("kubejs:lyra"));
        helper.assertTrue(PerkTree.getInstance().getRootPerk(LogicalSide.SERVER, lyra) != null, "lyra has no root perk in the perk tree");
        helper.succeed();
    }
}
