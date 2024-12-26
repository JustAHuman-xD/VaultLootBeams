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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemList {
    protected final Set<String> dynamicKeys;
    protected final Set<Item> allDynamic;
    protected final Set<Item> items;
    protected final Set<TagKey<Item>> tags;
    protected final Set<String> modIds;

    public ItemList() {
        this(new LinkedHashMap<>(), new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    private ItemList(Map<String, Set<Item>> dynamic, Set<Item> items, Set<TagKey<Item>> tags, Set<String> modIds) {
        this.dynamicKeys = new LinkedHashSet<>(dynamic.keySet());
        this.allDynamic = new LinkedHashSet<>(dynamic.values().stream().reduce(new LinkedHashSet<>(), (a, b) -> {
            a.addAll(b);
            return a;
        }));
        this.items = items;
        this.tags = tags;
        this.modIds = modIds;
    }

    public ItemList add(Item... items) {
        this.items.addAll(Arrays.asList(items));
        return this;
    }

    public ItemList add(TagKey<Item> tag) {
        tags.add(tag);
        return this;
    }

    public ItemList add(String modId) {
        modIds.add(modId);
        return this;
    }

    public boolean contains(Item item) {
        if (allDynamic.contains(item) || items.contains(item)) {
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

    public List<String> toStringList() {
        List<String> items = new ArrayList<>(this.dynamicKeys);

        for (Item item : this.items) {
            ResourceLocation registryName = item.getRegistryName();
            if (registryName == null) {
                VaultLootBeams.LOGGER.warn("Couldn't serialize item {}", item);
                continue;
            }
            items.add(registryName.toString());
        }

        for (TagKey<Item> tag : this.tags) {
            items.add("#" + tag.location());
        }

        items.addAll(this.modIds);

        return items;
    }

    public JsonArray serialize() {
        JsonArray items = new JsonArray();
        for (String item : toStringList()) {
            items.add(item);
        }
        return items;
    }

    public static ItemList deserialize(JsonObject root, String key, ItemList def) {
        if (!(root.get(key) instanceof JsonArray serializedItems)) {
            VaultLootBeams.LOGGER.warn("No/Invalid item list found for {}, using default", key);
            return def;
        }

        List<String> serialized = new ArrayList<>();
        for (JsonElement element : serializedItems) {
            if (!(element instanceof JsonPrimitive primitive) || !primitive.isString()) {
                VaultLootBeams.LOGGER.warn("Invalid item entry: {}", element);
                continue;
            }
            serialized.add(primitive.getAsString());
        }

        return deserialize(key, serialized);
    }

    public static ItemList deserialize(String key, List<String> serialized) {
        Utils.enableWarnings();
        Map<String, Set<Item>> dynamic = new LinkedHashMap<>();
        Set<Item> items = new LinkedHashSet<>();
        Set<TagKey<Item>> tags = new LinkedHashSet<>();
        Set<String> modIds = new LinkedHashSet<>();

        for (String itemKey : serialized) {
            if (itemKey.startsWith("#")) {
                TagKey<Item> tag = Utils.getTag(key, itemKey);
                if (tag != null) {
                    tags.add(tag);
                }
            } else if (!itemKey.contains(":")) {
                if (Utils.isModId(key, itemKey)) {
                    modIds.add(itemKey);
                }
            } else if (itemKey.contains("*")) {
                List<Item> matching = Utils.getMatchingItems(key, itemKey);
                if (!matching.isEmpty()) {
                    dynamic.put(itemKey, new LinkedHashSet<>(matching));
                }
            } else {
                Item item = Utils.getItem(key, itemKey);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return new ItemList(dynamic, items, tags, modIds);
    }
}
