package nadiendev.kubejsastralsorcery.recipe.kube;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.rhino.Context;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static nadiendev.kubejsastralsorcery.recipe.schema.AstralSchemas.*;

public class FocalTransmutationKubeRecipe extends AstralKubeRecipe {
    public KubeRecipe state(Context cx, Object state) {
        return addWrapped(cx, TRANSMUTATION_OUTPUTS, state);
    }

    public KubeRecipe state(Context cx, Object state, int weight) {
        WeightedEntry.Wrapper<BlockState> wrapped = wrapElement(cx, TRANSMUTATION_OUTPUTS, state);
        return addTo(TRANSMUTATION_OUTPUTS, WeightedEntry.wrap(wrapped.data(), weight));
    }

    public KubeRecipe input(Context cx, Object predicate) {
        return addWrapped(cx, TRANSMUTATION_INPUTS, predicate);
    }

    @Override
    public void serialize() {
        Ingredient display = getValue(TRANSMUTATION_DISPLAY);

        if (display == null || display.isEmpty()) {
            Set<Item> items = new LinkedHashSet<>();
            List<BlockPredicate> predicates = getValue(TRANSMUTATION_INPUTS);

            if (predicates != null) {
                for (BlockPredicate predicate : predicates) {
                    JsonElement json = BlockPredicate.CODEC.encodeStart(type.event.ops.json(), predicate).result().orElse(null);
                    collectBlockItems(json, items);
                }
            }

            items.remove(Items.AIR);

            if (!items.isEmpty()) {
                setValue(TRANSMUTATION_DISPLAY, Ingredient.of(items.toArray(Item[]::new)));
            }
        }

        super.serialize();
    }

    private static void collectBlockItems(JsonElement json, Set<Item> items) {
        if (json == null || !json.isJsonObject()) {
            return;
        }

        JsonObject obj = json.getAsJsonObject();
        String type = obj.has("type") ? obj.get("type").getAsString() : "";

        if (type.equals("minecraft:matching_blocks") && obj.has("blocks")) {
            JsonElement blocks = obj.get("blocks");
            JsonArray array = new JsonArray();

            if (blocks.isJsonArray()) {
                array = blocks.getAsJsonArray();
            } else {
                array.add(blocks);
            }

            for (JsonElement block : array) {
                String id = block.getAsString();

                if (!id.startsWith("#")) {
                    BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id)).ifPresent(b -> items.add(b.asItem()));
                }
            }
        } else if (type.equals("minecraft:any_of") || type.equals("minecraft:all_of")) {
            obj.getAsJsonArray("predicates").forEach(p -> collectBlockItems(p, items));
        }
    }
}
