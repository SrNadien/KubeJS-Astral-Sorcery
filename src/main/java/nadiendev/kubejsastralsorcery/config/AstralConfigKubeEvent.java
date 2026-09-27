package nadiendev.kubejsastralsorcery.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.ListJS;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AstralConfigKubeEvent implements KubeEvent {
    private final Map<String, ModConfigSpec.ConfigValue<?>> values = new TreeMap<>();

    public AstralConfigKubeEvent() {
        for (ModConfig config : ModConfigs.getModConfigs(AstralSorceryKJS.AS_ID)) {
            if (config.getSpec() instanceof ModConfigSpec spec && spec.isLoaded()) {
                collect(spec.getValues(), "");
            }
        }
    }

    private void collect(UnmodifiableConfig config, String prefix) {
        for (Map.Entry<String, Object> entry : config.valueMap().entrySet()) {
            String path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();

            if (entry.getValue() instanceof ModConfigSpec.ConfigValue<?> value) {
                values.put(path, value);
            } else if (entry.getValue() instanceof UnmodifiableConfig child) {
                collect(child, path);
            }
        }
    }

    private ModConfigSpec.ConfigValue<?> value(String path) {
        ModConfigSpec.ConfigValue<?> value = values.get(path);

        if (value == null) {
            throw new IllegalArgumentException("Unknown Astral Sorcery config value '" + path + "'. Loaded values: " + values.keySet());
        }

        return value;
    }

    public List<String> getPaths() {
        return new ArrayList<>(values.keySet());
    }

    public boolean has(String path) {
        return values.containsKey(path);
    }

    public Object get(String path) {
        return value(path).get();
    }

    public Object getDefault(String path) {
        return value(path).getDefault();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public AstralConfigKubeEvent set(String path, Object newValue) {
        ModConfigSpec.ConfigValue value = value(path);
        Object current = value.get();
        Object converted = convert(current, newValue, path);
        value.set(converted);
        AstralSorceryKJS.LOGGER.debug("Astral Sorcery config {} = {}", path, converted);
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public AstralConfigKubeEvent reset(String path) {
        ModConfigSpec.ConfigValue value = value(path);
        value.set(value.getDefault());
        return this;
    }

    private static Object convert(Object current, Object value, String path) {
        if (current instanceof Integer) {
            return ((Number) value).intValue();
        } else if (current instanceof Long) {
            return ((Number) value).longValue();
        } else if (current instanceof Double) {
            return ((Number) value).doubleValue();
        } else if (current instanceof Float) {
            return ((Number) value).floatValue();
        } else if (current instanceof Boolean) {
            return value instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(value));
        } else if (current instanceof String) {
            return String.valueOf(value);
        } else if (current instanceof List<?>) {
            List<String> list = new ArrayList<>();
            ListJS.orSelf(value).forEach(o -> list.add(String.valueOf(o)));
            return list;
        } else if (current instanceof Enum<?> e) {
            return Enum.valueOf(e.getDeclaringClass(), String.valueOf(value).toUpperCase());
        }

        throw new IllegalArgumentException("Config value '" + path + "' has an unsupported type " + (current == null ? "null" : current.getClass().getSimpleName()));
    }
}
