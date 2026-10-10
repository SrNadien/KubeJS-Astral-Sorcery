package nadiendev.kubejsastralsorcery.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import hellfirepvp.astralsorcery.common.recipe.altar.AltarRecipe;
import hellfirepvp.astralsorcery.common.util.data.IntRange;
import nadiendev.kubejsastralsorcery.AstralSorceryKJS;

public final class AstralFormat {
    private static Boolean snakeRecipes;
    private static Boolean snakeRanges;

    private AstralFormat() {
    }

    public static boolean snakeRecipes() {
        if (snakeRecipes == null) {
            snakeRecipes = AltarRecipe.CODEC.keys(JsonOps.INSTANCE).anyMatch(key -> "required_type".equals(key.getAsString()));
            AstralSorceryKJS.LOGGER.info("Astral Sorcery recipe keys use {}", snakeRecipes ? "snake_case" : "camelCase");
        }

        return snakeRecipes;
    }

    public static boolean snakeRanges() {
        if (snakeRanges == null) {
            JsonElement json = IntRange.CODEC.encodeStart(JsonOps.INSTANCE, IntRange.of(0, 1)).result().orElse(null);
            snakeRanges = json instanceof JsonObject obj && obj.has("min_inclusive");
        }

        return snakeRanges;
    }

    public static String key(String camel) {
        return snakeRecipes() ? snake(camel) : camel;
    }

    public static String rangeKey(String camel) {
        return snakeRanges() ? snake(camel) : camel;
    }

    public static String other(String name) {
        return name.indexOf('_') >= 0 ? camel(name) : snake(name);
    }

    public static String snake(String camel) {
        StringBuilder sb = new StringBuilder(camel.length() + 4);

        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);

            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public static String camel(String snake) {
        StringBuilder sb = new StringBuilder(snake.length());
        boolean upper = false;

        for (int i = 0; i < snake.length(); i++) {
            char c = snake.charAt(i);

            if (c == '_') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }

        return sb.toString();
    }

    public static JsonElement get(JsonObject json, String camel) {
        JsonElement value = json.get(camel);
        return value != null ? value : json.get(snake(camel));
    }

    public static boolean has(JsonObject json, String camel) {
        return json.has(camel) || json.has(snake(camel));
    }
}
