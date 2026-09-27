package nadiendev.kubejsastralsorcery.client;

import hellfirepvp.astralsorcery.client.screen.ScreenConstellationPaper;
import hellfirepvp.astralsorcery.client.screen.tome.TomePagesScreen;
import hellfirepvp.astralsorcery.common.constellation.BaseConstellation;
import hellfirepvp.astralsorcery.common.container.provider.ContainerAltarProvider;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.recipe.altar.AltarRecipe;
import hellfirepvp.astralsorcery.common.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.research.ResearchNode;
import hellfirepvp.astralsorcery.common.research.data.ResearchNodeLoader;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import hellfirepvp.observerlib.api.util.StructureBlockArray;
import hellfirepvp.observerlib.common.change.ObserverProviderStructure;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import nadiendev.kubejsastralsorcery.altar.AltarRestrictions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AstralShowcase {
    private static final String WORLD = "AstralShowcase";
    private static final int WAIT = 40;

    private record Step(String name, int delay, Runnable setup) {
    }

    private static final List<Step> STEPS = new ArrayList<>();
    private static int ticks;
    private static int index;
    private static int stepStart = -1;
    private static boolean worldRequested;
    private static BlockPos origin;

    private AstralShowcase() {
    }

    public static boolean requested() {
        return System.getProperty("kubejsastralsorcery.showcase") != null;
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(AstralShowcase::tick);
    }

    private static void check(String line) {
        try {
            Path file = Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots/astral_checks.txt");
            Files.createDirectories(file.getParent());
            Files.writeString(file, line + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            AstralSorceryKJS.LOGGER.warn("Could not write showcase check", ex);
        }
    }

    private static void openWorld(Minecraft mc) {
        worldRequested = true;

        if (mc.getLevelSource().levelExists(WORLD)) {
            mc.createWorldOpenFlows().openWorld(WORLD, () -> mc.setScreen(new TitleScreen()));
            return;
        }

        GameRules rules = new GameRules();
        rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
        LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(1234L, false, false),
            access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), new TitleScreen());
    }

    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            if (!worldRequested && (mc.screen instanceof TitleScreen || mc.screen instanceof AccessibilityOnboardingScreen)) {
                mc.options.onboardAccessibility = false;
                openWorld(mc);
            }

            return;
        }

        MinecraftServer server = mc.getSingleplayerServer();

        if (mc.player == null || server == null) {
            return;
        }

        ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());

        if (player == null) {
            return;
        }

        ticks++;

        if (mc.screen instanceof PauseScreen) {
            mc.setScreen(null);
        }

        if (ticks == 40) {
            mc.options.pauseOnLostFocus = false;
            mc.options.tutorialStep = TutorialSteps.NONE;
            mc.getTutorial().setStep(TutorialSteps.NONE);
            server.execute(() -> prepare(player));
            return;
        }

        if (ticks == 100) {
            buildSteps(mc, server, player);
            stepStart = ticks;
            return;
        }

        if (stepStart < 0 || ticks < stepStart) {
            return;
        }

        if (index >= STEPS.size()) {
            if (ticks == stepStart) {
                check("showcase finished");
                mc.stop();
            }

            return;
        }

        Step step = STEPS.get(index);

        if (ticks == stepStart) {
            try {
                step.setup().run();
            } catch (Exception ex) {
                check(step.name() + ": setup failed " + ex);
                AstralSorceryKJS.LOGGER.error("Showcase step {} failed", step.name(), ex);
            }
        } else if (ticks == stepStart + step.delay()) {
            Screenshot.grab(mc.gameDirectory, "astral_" + String.format("%02d", index) + "_" + step.name() + ".png", mc.getMainRenderTarget(), message -> {
            });
            index++;
            stepStart = ticks + 5;
        }
    }

    private static void prepare(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ResearchHelper.maximizeAll(player);
        level.setDayTime(18000);
        origin = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-12, 0, 2), origin.offset(28, 12, 30))) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }

        build(level, ResourceLocation.parse("kubejs:stellar_altar"), origin.offset(0, 0, 14));
        build(level, ResourceLocation.parse("kubejs:celestial_altar"), origin.offset(14, 0, 14));
        player.getInventory().add(new ItemStack(Items.FEATHER, 16));
    }

    private static void build(ServerLevel level, ResourceLocation id, BlockPos pos) {
        StructureBlockArray structure = (StructureBlockArray) ((ObserverProviderStructure) RegistryProviders.getProvider(id)).getStructure();
        structure.place(level, pos);
    }

    private static void buildSteps(Minecraft mc, MinecraftServer server, ServerPlayer player) {
        STEPS.clear();

        STEPS.add(new Step("altars", 60, () -> {
            mc.options.hideGui = true;
            server.execute(() -> player.teleportTo(player.serverLevel(), origin.getX() + 7.5, origin.getY() + 6, origin.getZ() + 1.5, 0F, 22F));
        }));

        STEPS.add(new Step("stellar_altar_close", WAIT, () -> {
            mc.getToasts().clear();
            server.execute(() -> player.teleportTo(player.serverLevel(), origin.getX() + 0.5, origin.getY() + 2.5, origin.getZ() + 10.5, 0F, 35F));
        }));

        STEPS.add(new Step("celestial_altar_close", WAIT, () -> server.execute(() -> player.teleportTo(player.serverLevel(), origin.getX() + 14.5, origin.getY() + 2.5, origin.getZ() + 10.5, 0F, 35F))));

        STEPS.add(new Step("altar_gui", WAIT, () -> server.execute(() -> {
            player.teleportTo(player.serverLevel(), origin.getX() + 0.5, origin.getY() + 1, origin.getZ() + 12.5, 0F, 20F);
            TileAltar altar = (TileAltar) player.serverLevel().getBlockEntity(origin.offset(0, 0, 14));
            ItemStack f = new ItemStack(Items.FEATHER);
            ItemStack[] grid = {f, f, f, f, new ItemStack(Items.DIAMOND), f, f, f, f};

            for (int i = 0; i < 9; i++) {
                altar.getTileData().getAltarInventory().setStackInSlot(i, grid[i].copy());
            }

            altar.getTileData().markForUpdate();
            ContainerAltarProvider.openAltar(altar).open(player);
            server.execute(() -> check("stellar altar tier on server: " + altar.getTileData().getAltarType() + ", structure formed: " + altar.hasStructure()));
        })));

        STEPS.add(new Step("tome_node_start", WAIT, () -> {
            mc.player.closeContainer();
            mc.options.hideGui = false;
            node("kubejs:starry_diamond").ifPresentOrElse(node -> {
                check("client node kubejs:starry_diamond title: " + node.getName().getString() + ", pages: " + node.getPages().size());
                mc.setScreen(TomePagesScreen.fromResearchNode(node, 0));
            }, () -> check("client node kubejs:starry_diamond MISSING"));
        }));

        STEPS.add(new Step("tome_node_constellation", WAIT, () -> node("kubejs:starry_diamond").ifPresent(node -> mc.setScreen(TomePagesScreen.fromResearchNode(node, 2)))));
        STEPS.add(new Step("tome_node_structure", WAIT, () -> node("kubejs:starry_diamond").ifPresent(node -> mc.setScreen(TomePagesScreen.fromResearchNode(node, 4)))));

        STEPS.add(new Step("tome_stellar_recipe", WAIT, () -> node("kubejs:stellar_lore").ifPresentOrElse(
            node -> mc.setScreen(TomePagesScreen.fromResearchNode(node, 0)),
            () -> check("client node kubejs:stellar_lore MISSING"))));

        STEPS.add(new Step("tome_welcome_extra_page", WAIT, () -> node("astralsorcery:welcome").ifPresent(node -> {
            int last = node.getPages().size() - 1;
            mc.setScreen(TomePagesScreen.fromResearchNode(node, last - (last % 2)));
        })));

        BaseConstellation lyra = RegistriesAS.REGISTRY_CONSTELLATIONS.get(ResourceLocation.parse("kubejs:lyra"));

        STEPS.add(new Step("paper_lyra", WAIT, () -> {
            check("client constellation lyra: " + (lyra == null ? "MISSING" : lyra.getName().getString() + " stars " + lyra.getStars().size()));

            if (lyra != null) {
                mc.setScreen(new ScreenConstellationPaper(lyra));
            }
        }));

        STEPS.add(new Step("tome_lyra", WAIT, () -> {
            if (lyra != null) {
                mc.setScreen(TomePagesScreen.fromConstellation(null, lyra));
            }
        }));

        STEPS.add(new Step("sky", 60, () -> {
            mc.setScreen(null);
            mc.options.hideGui = true;
            server.execute(() -> player.teleportTo(player.serverLevel(), origin.getX() + 0.5, origin.getY() + 1, origin.getZ() - 20.5, 180F, -70F));
            checkRecipes(mc);
        }));
    }

    private static Optional<ResearchNode> node(String id) {
        return ResearchNodeLoader.getInstance().getNode(ResourceLocation.parse(id));
    }

    private static void checkRecipes(Minecraft mc) {
        var manager = mc.getConnection().getRecipeManager();
        manager.byKey(ResourceLocation.parse("kubejs:test/altar_stellar")).ifPresentOrElse(
            holder -> check("client recipe altar_stellar tier: " + ((AltarRecipe) holder.value()).getRequiredType()),
            () -> check("client recipe altar_stellar MISSING"));
        manager.byKey(ResourceLocation.parse("kubejs:test/altar_exclusive")).ifPresentOrElse(
            holder -> check("client recipe altar_exclusive altar: " + AltarRestrictions.requiredAltar((AltarRecipe) holder.value())),
            () -> check("client recipe altar_exclusive MISSING"));
    }
}
