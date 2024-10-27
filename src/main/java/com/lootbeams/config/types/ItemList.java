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
import java.util.List;

public class ItemList {
    protected final List<Item> items;
    protected final List<TagKey<Item>> tags;
    protected final List<String> modIds;

    public ItemList() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private ItemList(List<Item> items, List<TagKey<Item>> tags, List<String> modIds) {
        this.items = items;
        this.tags = tags;
        this.modIds = modIds;
    }

    public boolean contains(Item item) {
        if (items.contains(item)) {
            return true;
        }

        ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        if (tagManager != null) {
            for (TagKey<Item> tag : tags) {
                if (tagManager.getTag(tag).contains(item)) {
                    return true;
                }
            }
        }

        ResourceLocation registryName = item.getRegistryName();
        if (registryName == null) {
            return false;
        }

        for (String modId : modIds) {
            if (registryName.getNamespace().equals(modId)) {
                return true;
            }
        }
        return false;
    }

    public static ItemList deserialize(JsonObject root, String key, ItemList def) {
        if (!(root.get(key) instanceof JsonArray serializedItems)) {
            LootBeams.LOGGER.warn("No/Invalid item list found for {}, using default", key);
            return def;
        }

        List<Item> items = new ArrayList<>();
        List<TagKey<Item>> tags = new ArrayList<>();
        List<String> modIds = new ArrayList<>();

        for (JsonElement element : serializedItems) {
            if (!(element instanceof JsonPrimitive primitive) || !primitive.isString()) {
                LootBeams.LOGGER.warn("Invalid item entry: {}", element);
                continue;
            }

            String itemKey = primitive.getAsString();
            if (!itemKey.contains(":")) {
                if (Utils.isModId(key, itemKey)) {
                    modIds.add(itemKey);
                }
            } else if (itemKey.startsWith("#")) {
                TagKey<Item> tag = Utils.getTag(key, itemKey);
                if (tag != null) {
                    tags.add(tag);
                }
            } else {
                Item item = Utils.getItem(key, itemKey);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return new ItemList(items, tags, modIds);
    }

    public static JsonArray serialize(ItemList itemMap) {
        JsonArray items = new JsonArray();
        for (Item item : itemMap.items) {
            ResourceLocation registryName = item.getRegistryName();
            if (registryName == null) {
                LootBeams.LOGGER.warn("Couldn't serialize item {}", item);
                continue;
            }
            items.add(new JsonPrimitive(registryName.toString()));
        }

        for (TagKey<Item> tag : itemMap.tags) {
            items.add(new JsonPrimitive("#" + tag.location()));
        }

        for (String modId : itemMap.modIds) {
            items.add(new JsonPrimitive(modId));
        }

        return items;
    }
}
