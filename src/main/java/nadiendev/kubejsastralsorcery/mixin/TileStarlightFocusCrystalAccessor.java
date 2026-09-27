package nadiendev.kubejsastralsorcery.mixin;

import hellfirepvp.astralsorcery.common.tile.TileStarlightFocusCrystal;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileStarlightFocusCrystal.class, remap = false)
public interface TileStarlightFocusCrystalAccessor {
    @Mutable
    @Accessor("PLAYER_BOX")
    static void kubejsastralsorcery$setPlayerBox(AABB box) {
        throw new AssertionError();
    }
}
