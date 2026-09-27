package nadiendev.kubejsastralsorcery.altar;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryObjectStorage;
import hellfirepvp.astralsorcery.common.lib.TileEntitiesAS;
import hellfirepvp.observerlib.common.change.ObserverProviderStructure;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;

public final class AltarRegistration {
    private AltarRegistration() {
    }

    public static List<AltarBlockBuilder> builders() {
        List<AltarBlockBuilder> list = new ArrayList<>();

        for (BuilderBase<? extends Block> builder : RegistryObjectStorage.BLOCK) {
            if (builder instanceof AltarBlockBuilder altar) {
                list.add(altar);
            }
        }

        return list;
    }

    public static void onRegister(RegisterEvent event) {
        if (!event.getRegistryKey().equals(RegistryProviders.REGISTRY_KEY)) {
            return;
        }

        for (AltarBlockBuilder builder : builders()) {
            event.register(RegistryProviders.REGISTRY_KEY, builder.id, () -> {
                Block block = builder.get();
                return new ObserverProviderStructure(builder.structure.build(block, builder.baseTier));
            });
            AstralSorceryKJS.LOGGER.info("Registered altar structure {}", builder.id);
        }
    }

    public static void onBlockEntityBlocks(BlockEntityTypeAddBlocksEvent event) {
        List<Block> blocks = new ArrayList<>();

        for (AltarBlockBuilder builder : builders()) {
            blocks.add(builder.get());
        }

        if (!blocks.isEmpty()) {
            event.modify(TileEntitiesAS.ALTAR.type(), blocks.toArray(Block[]::new));
        }
    }
}
