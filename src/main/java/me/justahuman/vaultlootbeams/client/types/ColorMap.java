package me.justahuman.vaultlootbeams.client.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.justahuman.vaultlootbeams.VaultLootBeams;
import me.justahuman.vaultlootbeams.utils.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorMap {
    protected final Map<Item, List<Color>> itemColors;
    protected final Map<TagKey<Item>, List<Color>> tagColors;
    protected final Map<String, List<Color>> modColors;

    public ColorMap() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    private ColorMap(Map<Item, List<Color>> itemColors, Map<TagKey<Item>, List<Color>> tagColors, Map<String, List<Color>> modColors) {
        this.itemColors = itemColors;
        this.tagColors = tagColors;
        this.modColors = modColors;
    }

    public List<Color> get(Item item) {
        List<Color> colors = itemColors.get(item);
        if (colors != null) {
            return colors;
        }

        ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        if (tagManager != null) {
            for (TagKey<Item> tag : tagColors.keySet()) {
                if (tagManager.getTag(tag).contains(item)) {
                    return tagColors.get(tag);
                }
            }
        }

        ResourceLocation registryName = item.getRegistryName();
        if (registryName == null) {
            return null;
        }

        for (String modId : modColors.keySet()) {
            if (registryName.getNamespace().equals(modId)) {
                return modColors.get(modId);
            }
        }

        return null;
    }

    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        for (Map.Entry<Item, List<Color>> entry : this.itemColors.entrySet()) {
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(entry.getKey());
            if (itemKey == null) {
                VaultLootBeams.LOGGER.warn("Couldn't serialize custom color for item {}", entry.getKey());
                continue;
            }
            object.add(itemKey.toString(), serializeColors(entry.getValue()));
        }

        for (Map.Entry<TagKey<Item>, List<Color>> entry : this.tagColors.entrySet()) {
            object.add("#" + entry.getKey().location(), serializeColors(entry.getValue()));
        }

        for (Map.Entry<String, List<Color>> entry : this.modColors.entrySet()) {
            object.add(entry.getKey(), serializeColors(entry.getValue()));
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
        Map<Item, List<Color>> itemColors = new HashMap<>();
        Map<TagKey<Item>, List<Color>> tagColors = new HashMap<>();
        Map<String, List<Color>> modColors = new HashMap<>();
        for (String itemKey : serializedColors.keySet()) {
            List<Color> colors = getColors(key, itemKey, serializedColors.get(itemKey));
             if (itemKey.startsWith("#")) {
                TagKey<Item> tag = Utils.getTag(key, itemKey);
                if (tag != null && colors != null && !colors.isEmpty()) {
                    tagColors.put(tag, colors);
                }
            } else if (!itemKey.contains(":")) {
                if (Utils.isModId(key, itemKey) && colors != null && !colors.isEmpty()) {
                    modColors.put(itemKey, colors);
                }
            } else {
                Item item = Utils.getItem(key, itemKey);
                if (item != null && colors != null && !colors.isEmpty()) {
                    itemColors.put(item, colors);
                }
            }
        }
        return new ColorMap(itemColors, tagColors, modColors);
    }

    private static List<Color> getColors(String key, String itemKey, JsonElement colors) {
        if (colors instanceof JsonArray array) {
            List<Color> colorList = new ArrayList<>();
            for (JsonElement element : array) {
                if (!(element instanceof JsonPrimitive primitive)) {
                    VaultLootBeams.LOGGER.warn("Invalid color entry for item \"{}\" in {}", itemKey, key);
                    continue;
                }

                Color color = getColor(key, itemKey, primitive);
                if (color != null) {
                    colorList.add(color);
                }
            }
            return colorList;
        } else if (colors instanceof JsonPrimitive primitive) {
            Color color = getColor(key, itemKey, primitive);
            if (color != null) {
                return List.of(color);
            }
        } else {
            VaultLootBeams.LOGGER.warn("Invalid colors for item \"{}\" in {}", itemKey, key);
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

    private static String serialize(Color color) {
        return String.format("0x%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }
}
