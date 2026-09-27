package nadiendev.kubejsastralsorcery.altar;

import hellfirepvp.astralsorcery.common.block.tile.AltarBlock;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import hellfirepvp.astralsorcery.common.util.data.ObserverRegistryObject;
import net.minecraft.resources.ResourceLocation;

public class KubeAltarBlock extends AltarBlock {
    private final ResourceLocation id;
    private final boolean exclusive;
    private final AltarStructureDefinition structure;
    private ObserverRegistryObject observer;

    public KubeAltarBlock(Properties properties, TileAltar.AltarType type, ResourceLocation id, boolean exclusive, AltarStructureDefinition structure) {
        super(properties, type);
        this.id = id;
        this.exclusive = exclusive;
        this.structure = structure;
    }

    public ResourceLocation getAltarId() {
        return id;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public AltarStructureDefinition getStructure() {
        return structure;
    }

    public ObserverRegistryObject getObserver() {
        if (observer == null) {
            observer = AltarObservers.of(id);
        }

        return observer;
    }
}
