package nadiendev.kubejsastralsorcery.altar;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.rhino.util.HideFromJS;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Locale;
import java.util.Map;

public class AltarBlockBuilder extends BlockBuilder {
    public transient String baseTier = "radiance";
    public transient String realTier;
    public transient boolean exclusive;
    public transient final AltarStructureDefinition structure = new AltarStructureDefinition();

    public AltarBlockBuilder(ResourceLocation id) {
        super(id);
        parentModel = AstralSorceryKJS.as("block/altar_radiance");
    }

    public AltarBlockBuilder baseTier(String tier) {
        String t = tier.toLowerCase(Locale.ROOT);

        if (!t.equals("illumination") && !t.equals("resonance") && !t.equals("luminance") && !t.equals("radiance")) {
            throw new IllegalArgumentException("Base tier must be illumination, resonance, luminance or radiance, got '" + tier + "'");
        }

        this.baseTier = t;
        this.parentModel = AstralSorceryKJS.as("block/altar_" + t);
        return this;
    }

    public AltarBlockBuilder realTier(String tier) {
        this.realTier = tier.toLowerCase(Locale.ROOT);
        return this;
    }

    public AltarBlockBuilder exclusive() {
        this.exclusive = true;
        return this;
    }

    public AltarBlockBuilder structureFrom(String base) {
        structure.base = base.toLowerCase(Locale.ROOT);
        return this;
    }

    public AltarBlockBuilder noBaseStructure() {
        structure.base = "none";
        return this;
    }

    public AltarBlockBuilder structureBlock(int x, int y, int z, String block) {
        if (x == 0 && y == 0 && z == 0) {
            throw new IllegalArgumentException("0, 0, 0 is the altar itself");
        }

        structure.entries.put(new BlockPos(x, y, z), block);
        return this;
    }

    public AltarBlockBuilder structureLayer(int y, String[] rows, Map<String, String> key) {
        int depth = rows.length;

        for (int row = 0; row < depth; row++) {
            String line = rows[row];
            int width = line.length();

            for (int column = 0; column < width; column++) {
                char c = line.charAt(column);

                if (c == ' ') {
                    continue;
                }

                int x = column - width / 2;
                int z = row - depth / 2;

                if (x == 0 && y == 0 && z == 0) {
                    continue;
                }

                String spec = c == '_' ? "air" : key.get(String.valueOf(c));

                if (spec == null) {
                    throw new IllegalArgumentException("Structure character '" + c + "' has no entry in the key");
                }

                structure.entries.put(new BlockPos(x, y, z), spec);
            }
        }

        return this;
    }

    @HideFromJS
    public TileAltar.AltarType altarType() {
        String name = (realTier != null ? realTier : baseTier).toUpperCase(Locale.ROOT);

        for (TileAltar.AltarType type : TileAltar.AltarType.values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }

        throw new IllegalStateException("Altar tier '" + realTier + "' does not exist. Real tiers must be declared in AstralSorceryEvents.altarTiers first.");
    }

    @Override
    @HideFromJS
    public Block createObject() {
        Block base = BuiltInRegistries.BLOCK.get(AstralSorceryKJS.as("altar_" + baseTier));
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(base);
        return new KubeAltarBlock(properties, altarType(), id, exclusive, structure);
    }
}
