package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.common.container.ContainerAltar;
import hellfirepvp.astralsorcery.common.research.ResearchTier;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import hellfirepvp.astralsorcery.common.util.data.ObserverRegistryObject;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(value = TileAltar.AltarType.class, remap = false)
public interface AltarTypeInvoker {
    @Invoker("<init>")
    static TileAltar.AltarType kubejsastralsorcery$create(String name, int ordinal, ResearchTier requiredTier, ObserverRegistryObject requiredStructure,
                                                        Supplier<? extends ItemLike> altarItemSupplier, VoxelShape shape, ContainerAltar.Type containerType) {
        throw new AssertionError();
    }
}
