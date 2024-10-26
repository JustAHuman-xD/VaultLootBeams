package com.lootbeams;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Utils {
    private static final Map<ItemEntity, Component> NAME_CACHE = new HashMap<>();
    private static final Map<ItemEntity, List<Component>> TOOLTIP_CACHE = new HashMap<>();

    public static boolean isRare(ItemEntity item) {
        return item.getItem().getRarity() != Rarity.COMMON;
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Returns the color from the item's name, rarity, tag, or override.
     */
    public static Color getItemColor(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (LootBeams.CRASH_BLACKLIST.contains(itemStack)) {
            return Color.WHITE;
        }

        try {
            Item item = itemStack.getItem();

            // From Config Overrides
            Color override = Configuration.colorOverride(item);
            if (override != null) {
                return override;
            }

            // From NBT
            CompoundTag tag = itemStack.getTag();
            if (tag != null && tag.contains("lootbeams.color", Tag.TAG_STRING)) {
                return Color.decode(tag.getString("lootbeams.color"));
            }

            // From Name
            if (Configuration.RENDER_NAME_COLOR.get()) {
                Color nameColor = getRawColor(nameCache(itemEntity, itemStack));
                if (!nameColor.equals(Color.WHITE)) {
                    return nameColor;
                }
            }

            // From Rarity
            if (Configuration.RENDER_RARITY_COLOR.get()) {
                Integer rarityColor = item.getRarity(itemStack).color.getColor();
                if (rarityColor != null) {
                    return new Color(rarityColor);
                }
            }
            return Color.WHITE;
        } catch (Exception e) {
            LootBeams.LOGGER.error("Failed to get color for ({}), added to temporary blacklist", itemStack.getDisplayName());
            LootBeams.CRASH_BLACKLIST.add(itemStack);
            LootBeams.LOGGER.info("Temporary blacklist is now : ");
            for (ItemStack stack : LootBeams.CRASH_BLACKLIST) {
                LootBeams.LOGGER.info(stack.getDisplayName());
            }
            return Color.WHITE;
        }
    }

    /**
     * Gets color from the first letter in the text component.
     */
    public static Color getRawColor(Component text) {
        List<Style> list = new ArrayList<>();

        text.visit((acceptor, styleIn) -> {
            StringDecomposer.iterateFormatted(styleIn, acceptor, (string, style, consumer) -> {
                list.add(style);
                return true;
            });
            return Optional.empty();
        }, Style.EMPTY);

        if (!list.isEmpty()) {
            TextColor color = list.get(0).getColor();
            if (color != null) {
                return new Color(color.getValue());
            }
        }

        return Color.WHITE;
    }

    /**
     * Checks if the player is looking at the given entity, accuracy determines how close the player has to look.
     */
    public static boolean isLookingAt(LocalPlayer player, Entity target, double accuracy) {
        Vec3 difference = new Vec3(target.getX() - player.getX(), target.getEyeY() - player.getEyeY(), target.getZ() - player.getZ());
        double length = difference.length();
        double dot = player.getViewVector(1.0F).normalize().dot(difference.normalize());
        return dot > 1.0D - accuracy / length && !target.isInvisible();
    }

    public static void cache(ItemEntity ie) {
        nameCache(ie, ie.getItem());
        tooltipCache(ie, ie.getItem());
    }

    public static Component nameCache(ItemEntity ie, ItemStack itemStack) {
        return NAME_CACHE.computeIfAbsent(ie, o1 -> itemStack.getHoverName());
    }

    public static List<Component> tooltipCache(ItemEntity ie, ItemStack itemStack) {
        return TOOLTIP_CACHE.computeIfAbsent(ie, o1 -> itemStack.getTooltipLines(null, TooltipFlag.Default.NORMAL));
    }

    public static void unCache(ItemEntity ie) {
        NAME_CACHE.remove(ie);
        TOOLTIP_CACHE.remove(ie);
    }

    /**
     * @return If the {@link Item} is an "Equipment Item"
     */
    public static boolean isEquipmentItem(Item item) {
        return item instanceof TieredItem
                || item instanceof ArmorItem
                || item instanceof ShieldItem
                || item instanceof BowItem
                || item instanceof CrossbowItem;
    }

    /**
     * @return Checks if the given {@link Item} is in the given {@link List} of registry names.
     */
    public static boolean isItemInRegistryList(List<String> registryNames, Item item) {
        if (registryNames.isEmpty()) {
            return false;
        }

        ResourceLocation itemResource = ForgeRegistries.ITEMS.getKey(item);
        if (itemResource == null) {
            return false;
        }

        for (String id : registryNames.stream().filter(s -> !s.isEmpty()).toList()) {
            if (!id.contains(":")  && itemResource.getNamespace().equals(id)) {
                return true;
            } else if (itemResource.equals(ResourceLocation.tryParse(id))) {
                return true;
            }
        }

        return false;
    }
}
