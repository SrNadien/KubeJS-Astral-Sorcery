package nadiendev.kubejsastralsorcery.altar;

import hellfirepvp.astralsorcery.common.structure.PatternAltarT2;
import hellfirepvp.astralsorcery.common.structure.PatternAltarT2Expanded;
import hellfirepvp.astralsorcery.common.structure.PatternAltarT3;
import hellfirepvp.astralsorcery.common.structure.PatternAltarT4;
import hellfirepvp.observerlib.api.block.MatchableState;
import hellfirepvp.observerlib.api.block.SimpleMatchableBlock;
import hellfirepvp.observerlib.api.block.SimpleMatchableBlockState;
import hellfirepvp.observerlib.api.util.StructureBlockArray;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AltarStructureDefinition {
    public String base = null;
    public final Map<BlockPos, String> entries = new LinkedHashMap<>();

    public StructureBlockArray build(Block altar, String defaultBase) {
        String from = (base == null ? defaultBase : base).toLowerCase(Locale.ROOT);
        StructureBlockArray array = switch (from) {
            case "resonance" -> new PatternAltarT2();
            case "resonance_expanded" -> new PatternAltarT2Expanded();
            case "luminance" -> new PatternAltarT3();
            case "radiance" -> new PatternAltarT4();
            case "illumination", "none" -> new StructureBlockArray();
            default -> throw new IllegalArgumentException("Unknown base structure '" + from + "', use none, resonance, resonance_expanded, luminance or radiance");
        };

        array.addBlock(altar.defaultBlockState(), 0, 0, 0);

        for (Map.Entry<BlockPos, String> entry : entries.entrySet()) {
            MatchableState state = parse(entry.getValue());

            if (state == null) {
                array.getContents().remove(entry.getKey());
            } else {
                array.addBlock(state, entry.getKey());
            }
        }

        return array;
    }

    public static MatchableState parse(String spec) {
        String s = spec.trim();

        if (s.equals("*") || s.equals("any") || s.isEmpty()) {
            return null;
        }

        if (s.equals("air") || s.equals("_") || s.equals("minecraft:air")) {
            return MatchableState.REQUIRES_AIR;
        }

        if (s.startsWith("#")) {
            return new TagState(TagKey.create(Registries.BLOCK, ResourceLocation.parse(s.substring(1))));
        }

        if (s.contains("|")) {
            List<Block> blocks = new ArrayList<>();

            for (String part : s.split("\\|")) {
                blocks.add(block(part.trim()));
            }

            return new SimpleMatchableBlock(blocks);
        }

        if (s.contains("[")) {
            try {
                BlockState state = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), s, false).blockState();
                return new SimpleMatchableBlockState(state);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid block state '" + s + "' in altar structure", ex);
            }
        }

        return new SimpleMatchableBlock(block(s));
    }

    private static Block block(String id) {
        ResourceLocation rl = ResourceLocation.parse(id);
        return BuiltInRegistries.BLOCK.getOptional(rl).orElseThrow(() -> new IllegalArgumentException("Unknown block '" + id + "' in altar structure"));
    }

    public record TagState(TagKey<Block> tag) implements MatchableState {
        @Override
        public BlockState getDescriptiveState(long tick) {
            List<Block> blocks = new ArrayList<>();
            BuiltInRegistries.BLOCK.getTagOrEmpty(tag).forEach(h -> blocks.add(h.value()));

            if (blocks.isEmpty()) {
                return Blocks.BARRIER.defaultBlockState();
            }

            return blocks.get((int) ((tick / 20L) % blocks.size())).defaultBlockState();
        }

        @Override
        public boolean matches(BlockGetter level, BlockPos pos, BlockState state) {
            return state.is(tag);
        }
    }
}
