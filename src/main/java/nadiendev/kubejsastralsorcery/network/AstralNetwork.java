package nadiendev.kubejsastralsorcery.network;

import hellfirepvp.astralsorcery.common.network.play.PktSyncLumenBindingTypes;
import hellfirepvp.astralsorcery.common.network.play.PktSyncPerkTree;
import hellfirepvp.astralsorcery.common.network.play.PktSyncResearchNodes;
import hellfirepvp.astralsorcery.common.research.data.ResearchNodeLoader;
import nadiendev.kubejsastralsorcery.AstralSorceryEvents;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import nadiendev.kubejsastralsorcery.research.ResearchInjector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AstralNetwork {
    private AstralNetwork() {
    }

    public record ResearchTitles(Map<ResourceLocation, String> titles) implements CustomPacketPayload {
        public static final Type<ResearchTitles> TYPE = new Type<>(AstralSorceryKJS.id("research_titles"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ResearchTitles> STREAM_CODEC = ByteBufCodecs.<RegistryFriendlyByteBuf, ResourceLocation, String, Map<ResourceLocation, String>>map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.STRING_UTF8)
            .map(ResearchTitles::new, ResearchTitles::titles);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(ResearchTitles.TYPE, ResearchTitles.STREAM_CODEC, AstralNetwork::handleTitles);
    }

    private static void handleTitles(ResearchTitles payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ResearchInjector.setClientTitles(payload.titles());

            if (FMLEnvironment.dist.isClient()) {
                ClientHooks.resetTome();
            }
        });
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        List<ServerPlayer> players = event.getPlayer() != null ? List.of(event.getPlayer()) : event.getPlayerList().getPlayers();
        boolean reload = event.getPlayer() == null;

        if (reload) {
            AstralSorceryEvents.postServerTweaks();
        }

        for (ServerPlayer player : players) {
            PacketDistributor.sendToPlayer(player, new ResearchTitles(ResearchInjector.serverTitles()));

            if (reload) {
                PacketDistributor.sendToPlayer(player, new PktSyncResearchNodes.Request(new ArrayList<>(ResearchNodeLoader.getInstance().getNodes())));
                PacketDistributor.sendToPlayer(player, PktSyncLumenBindingTypes.newRequest());
                PacketDistributor.sendToPlayer(player, PktSyncPerkTree.sync());
            }
        }
    }
}
