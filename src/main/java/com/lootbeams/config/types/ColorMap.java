package com.lootbeams.config.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.lootbeams.LootBeams;
import com.lootbeams.utils.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorMap {
    protected final Map<Item, List<Integer>> itemColors;
    protected final Map<TagKey<Item>, List<Integer>> tagColors;
    protected final Map<String, List<Integer>> modColors;

    public ColorMap() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    private ColorMap(Map<Item, List<Integer>> itemColors, Map<TagKey<Item>, List<Integer>> tagColors, Map<String, List<Integer>> modColors) {
        this.itemColors = itemColors;
        this.tagColors = tagColors;
        this.modColors = modColors;
    }

    public List<Integer> get(Item item) {
        List<Integer> colors = itemColors.get(item);
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

    public static ColorMap deserialize(JsonObject root, String key, ColorMap def) {
        if (!(root.get(key) instanceof JsonObject serializedColors)) {
            LootBeams.LOGGER.warn("No/Invalid color map found for {}, using default", key);
            return def;
        }

        Map<Item, List<Integer>> itemColors = new HashMap<>();
        Map<TagKey<Item>, List<Integer>> tagColors = new HashMap<>();
        Map<String, List<Integer>> modColors = new HashMap<>();
        for (String itemKey : serializedColors.keySet()) {
            List<Integer> colors = getColors(itemKey, serializedColors.get(itemKey));
            if (!itemKey.contains(":")) {
                if (Utils.isModId(key, itemKey) && colors != null && !colors.isEmpty()) {
                    modColors.put(itemKey, colors);
                }
            } else if (itemKey.startsWith("#")) {
                TagKey<Item> tag = Utils.getTag(key, itemKey);
                if (tag != null && colors != null && !colors.isEmpty()) {
                    tagColors.put(tag, colors);
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

    private static List<Integer> getColors(String itemKey, JsonElement colors) {
        if (colors instanceof JsonArray array) {
            List<Integer> colorList = new ArrayList<>();
            for (JsonElement element : array) {
                if (!(element instanceof JsonPrimitive primitive)) {
                    LootBeams.LOGGER.warn("Invalid color entry for item \"{}\" in customColors", itemKey);
                    continue;
                }

                Integer color = getColor(itemKey, primitive);
                if (color != null) {
                    colorList.add(color);
                }
            }
            return colorList;
        } else if (colors instanceof JsonPrimitive primitive) {
            Integer color = getColor(itemKey, primitive);
            if (color != null) {
                return List.of(color);
            }
        } else {
            LootBeams.LOGGER.warn("Invalid colors for item \"{}\" in customColors", itemKey);
        }
        return null;
    }

    private static Integer getColor(String itemKey, JsonPrimitive primitive) {
        if (primitive.isNumber()) {
            return primitive.getAsInt();
        } else if (primitive.isString()) {
            return Integer.decode(primitive.getAsString());
        }
        LootBeams.LOGGER.warn("Invalid color for item \"{}\" in customColors", itemKey);
        return null;
    }

    public static JsonObject serialize(ColorMap customColors) {
        JsonObject object = new JsonObject();
        for (Map.Entry<Item, List<Integer>> entry : customColors.itemColors.entrySet()) {
            ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(entry.getKey());
            if (itemKey == null) {
                LootBeams.LOGGER.warn("Couldn't serialize custom color for item {}", entry.getKey());
                continue;
            }

            if (entry.getValue().size() == 1) {
                object.addProperty(itemKey.toString(), entry.getValue().get(0));
                continue;
            }

            JsonArray array = new JsonArray();
            for (Integer color : entry.getValue()) {
                array.add(new JsonPrimitive(Integer.toHexString(color)));
            }
            object.add(itemKey.toString(), array);
        }
        return object;
    }
}
