package me.justahuman.vaultlootbeams.client.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.justahuman.vaultlootbeams.VaultLootBeams;
import me.justahuman.vaultlootbeams.utils.Utils;
import net.minecraft.world.item.Item;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ColorMap {
    public final Map<Item, List<Color>> cache = new ConcurrentHashMap<>();
    public final List<ColorGroup> colors;

    public ColorMap() {
        this(new ArrayList<>());
    }

    private ColorMap(List<ColorGroup> colors) {
        this.colors = colors;
    }

    public ColorMap add(Item item, Color... color) {
        return add(list -> list.add(item), color);
    }

    public ColorMap add(Consumer<ItemList> items, Color... color) {
        ItemList itemList = new ItemList();
        items.accept(itemList);
        colors.add(new ColorGroup(itemList.toStringList().get(0), itemList, List.of(color)));
        return this;
    }

    public ColorMap add(String id, Consumer<ItemList> items, Color... color) {
        ItemList itemList = new ItemList();
        items.accept(itemList);
        colors.add(new ColorGroup(id, itemList, List.of(color)));
        return this;
    }

    public boolean contains(Item item) {
        List<Color> colors = get(item);
        return colors != null && !colors.isEmpty();
    }

    public List<Color> get(Item item) {
        return cache.computeIfAbsent(item, i -> {
            for (ColorGroup group : colors) {
                if (group.items.contains(i)) {
                    return group.colors;
                }
            }
            return null;
        });
    }

    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        for (ColorGroup group : this.colors) {
            List<String> items = group.items.toStringList();
            if (items.size() == 1) {
                object.add(items.get(0), serializeColors(group.colors));
                continue;
            }
            JsonObject groupObject = new JsonObject();
            JsonArray serializedItems = new JsonArray();
            items.forEach(serializedItems::add);
            groupObject.add("items", serializedItems);
            groupObject.add("colors", serializeColors(group.colors));
            object.add(group.id, groupObject);
        }
        return object;
    }

    private JsonElement serializeColors(List<Color> colors) {
        if (colors.size() == 1) {
            return new JsonPrimitive(serialize(colors.get(0)));
        }

        JsonArray array = new JsonArray();
        for (Color color : colors) {
            array.add(serialize(color));
        }
        return array;
    }

    public static ColorMap deserialize(JsonObject root, String key, ColorMap def) {
        if (!(root.get(key) instanceof JsonObject serializedColors)) {
            VaultLootBeams.LOGGER.warn("No/Invalid color map found for {}, using default", key);
            return def;
        }

        Utils.enableWarnings();
        List<ColorGroup> colors = new ArrayList<>();
        for (String groupId : serializedColors.keySet()) {
            JsonElement serialized = serializedColors.get(groupId);
            if (serialized instanceof JsonPrimitive || serialized instanceof JsonArray) {
                colors.add(new ColorGroup(groupId, ItemList.deserialize(key + ":" + groupId, List.of(groupId)), getColors(key, groupId, serialized)));
            } else if (serialized instanceof JsonObject group) {
                JsonElement serializedItems = group.get("items");
                JsonElement groupColors = group.get("colors");
                if (!(serializedItems instanceof JsonArray)) {
                    VaultLootBeams.LOGGER.warn("Invalid items for group \"{}\" in {}", groupId, key);
                    continue;
                }
                List<String> items = new ArrayList<>();
                for (JsonElement element : serializedItems.getAsJsonArray()) {
                    if (!(element instanceof JsonPrimitive primitive) || !primitive.isString()) {
                        VaultLootBeams.LOGGER.warn("Invalid item entry \"{}\" in group \"{}\" in {}", element, groupId, key);
                        continue;
                    }
                    items.add(primitive.getAsString());
                }
                colors.add(new ColorGroup(groupId, ItemList.deserialize(key + ":" + groupId, items), getColors(key, groupId, groupColors)));
            } else {
                VaultLootBeams.LOGGER.warn("Invalid color entry for item \"{}\" in {}", groupId, key);
            }
        }
        return new ColorMap(colors);
    }

    private static List<Color> getColors(String key, String groupId, JsonElement colors) {
        if (colors instanceof JsonArray array) {
            List<Color> colorList = new ArrayList<>();
            for (JsonElement element : array) {
                if (!(element instanceof JsonPrimitive primitive)) {
                    VaultLootBeams.LOGGER.warn("Invalid color entry for group \"{}\" in {}", groupId, key);
                    continue;
                }

                Color color = getColor(key, groupId, primitive);
                if (color != null) {
                    colorList.add(color);
                }
            }
            return colorList;
        } else if (colors instanceof JsonPrimitive primitive) {
            Color color = getColor(key, groupId, primitive);
            if (color != null) {
                return List.of(color);
            }
        } else {
            VaultLootBeams.LOGGER.warn("Invalid colors for group \"{}\" in {}", groupId, key);
        }
        return null;
    }

    private static Color getColor(String key, String itemKey, JsonPrimitive primitive) {
        if (primitive.isNumber()) {
            return new Color(primitive.getAsInt());
        } else if (primitive.isString()) {
            try {
                return Color.decode(primitive.getAsString());
            } catch (Exception e) {
                VaultLootBeams.LOGGER.warn("Invalid color for item \"{}\" in {}", itemKey, key);
            }
        }
        VaultLootBeams.LOGGER.warn("Invalid color for item \"{}\" in {}", itemKey, key);
        return null;
    }

    public static String serialize(Color color) {
        return String.format("0x%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static final class ColorGroup {
        public String id;
        public ItemList items;
        public List<Color> colors;

        public ColorGroup(String id, ItemList items, List<Color> colors) {
            this.id = id;
            this.items = items;
            this.colors = new ArrayList<>(colors);
        }
    }
}
