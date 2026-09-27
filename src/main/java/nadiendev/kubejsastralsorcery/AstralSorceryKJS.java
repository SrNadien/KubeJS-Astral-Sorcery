package nadiendev.kubejsastralsorcery;

import nadiendev.kubejsastralsorcery.altar.AltarRegistration;
import nadiendev.kubejsastralsorcery.client.AstralShowcase;
import nadiendev.kubejsastralsorcery.constellation.ConstellationData;
import nadiendev.kubejsastralsorcery.network.AstralNetwork;
import nadiendev.kubejsastralsorcery.recipe.AstralRecipeFilters;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AstralSorceryKJS.MOD_ID)
public final class AstralSorceryKJS {
    public static final String MOD_ID = "kubejsastralsorcery";
    public static final String AS_ID = "astralsorcery";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public AstralSorceryKJS(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(AstralRecipeFilters.class);
        NeoForge.EVENT_BUS.addListener(AstralNetwork::onDatapackSync);
        modBus.addListener(AstralNetwork::register);
        modBus.addListener(AltarRegistration::onRegister);
        modBus.addListener(ConstellationData::onRegister);
        modBus.addListener(AltarRegistration::onBlockEntityBlocks);

        if (FMLEnvironment.dist.isClient() && AstralShowcase.requested()) {
            AstralShowcase.register();
        }
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent event) -> AstralSorceryEvents.postServerTweaks());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static ResourceLocation as(String path) {
        return ResourceLocation.fromNamespaceAndPath(AS_ID, path);
    }
}
