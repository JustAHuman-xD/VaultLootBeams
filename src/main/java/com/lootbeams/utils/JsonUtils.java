package com.lootbeams.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.lootbeams.LootBeams;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JsonUtils {
    public static String get(JsonElement json, String key, String def) {
        return handle(json, obj -> obj.get(key) instanceof JsonPrimitive primitive
                && primitive.isString() ? primitive.getAsString() : def, def);
    }

    public static List<String> getList(JsonElement json, String key, List<String> def) {
        return handle(json, obj -> {
            if (obj.get(key) instanceof JsonArray array) {
                List<String> strings = new ArrayList<>();
                for (JsonElement element : array) {
                    if (element instanceof JsonPrimitive primitive && primitive.isString()) {
                        strings.add(primitive.getAsString());
                    }
                }
                return strings;
            }
            return def;
        }, def);
    }

    public static JsonArray serializeList(List<String> strings) {
        JsonArray array = new JsonArray();
        for (String string : strings) {
            array.add(new JsonPrimitive(string));
        }
        return array;
    }

    public static <E extends Enum<E>> E get(JsonElement json, String key, E def, Class<E> clazz) {
        return handle(json, obj -> obj.get(key) instanceof JsonPrimitive primitive
                && primitive.isString() ? Enum.valueOf(clazz, primitive.getAsString()) : def, def);
    }

    public static int get(JsonElement json, String key, int def) {
        return handle(json, obj -> obj.get(key) instanceof JsonPrimitive primitive
                && primitive.isNumber() ? primitive.getAsInt() : def, def);
    }

    public static int getBounded(JsonElement json, String key, int min, int max, int def) {
        int value = get(json, key, def);
        if (value > max || value < min) {
            warnBounds(key, value, min, max, def);
            return def;
        }
        return value;
    }

    public static double get(JsonElement json, String key, double def) {
        return handle(json, obj -> obj.get(key) instanceof JsonPrimitive primitive
                && primitive.isNumber() ? primitive.getAsDouble() : def, def);
    }

    public static double getBounded(JsonElement json, String key, double min, double max, double def) {
        double value = get(json, key, def);
        if (value > max || value < min) {
            warnBounds(key, value, min, max, def);
            return def;
        }
        return value;
    }

    public static float get(JsonElement json, String key, float def) {
        return handle(json, obj -> obj.get(key) instanceof JsonPrimitive primitive
                && primitive.isNumber() ? primitive.getAsFloat() : def, def);
    }

    public static float getBounded(JsonElement json, String key, float min, float max, float def) {
        float value = get(json, key, def);
        if (value > max || value < min) {
            warnBounds(key, value, min, max, def);
            return def;
        }
        return value;
    }

    public static boolean get(JsonElement json, String key, boolean def) {
        return handle(json, obj -> obj.get(key) instanceof JsonPrimitive primitive
                && primitive.isBoolean() ? primitive.getAsBoolean() : def, def);
    }

    public static <T> T get(JsonElement json, String key, @Nonnull T def) {
        return handle(json, obj -> def.getClass().isInstance(obj.get(key)) ? (T) obj.get(key) : def, def);
    }

    public static <T> T get(JsonElement json, String key, T def, Class<T> clazz) {
        return handle(json, obj -> clazz.isInstance(obj.get(key)) ? clazz.cast(obj.get(key)) : def, def);
    }

    private static void warnBounds(String key, Number value, Number min, Number max, Number def) {
        LootBeams.LOGGER.warn("Value \"{}\" for {} is out of bounds ({} - {}), using default \"{}\"", value, key, min, max, def);
    }

    private static <T> T handle(JsonElement json, Function<JsonObject, T> mapper, T def) {
        if (json instanceof JsonObject obj) {
            try {
                return mapper.apply(obj);
            } catch (Exception e) {
                return def;
            }
        }
        return def;
    }
}
