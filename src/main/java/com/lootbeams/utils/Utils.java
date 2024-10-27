package com.lootbeams.utils;

import com.lootbeams.LootBeams;
import com.lootbeams.config.types.ItemCondition;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.lootbeams.config.ModConfig.CONFIG;

public class Utils {
    public static final int WHITE = color(255, 255, 255, 255);
    private static final Map<ItemEntity, Component> NAME_CACHE = new HashMap<>();
    private static final Map<ItemEntity, List<Component>> TOOLTIP_CACHE = new HashMap<>();

    public static boolean rendersBeam(ItemEntity itemEntity) {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.distanceToSqr(itemEntity) <= Math.pow(CONFIG.renderDistance, 2)
                && passes(CONFIG.renderCondition, CONFIG.renderWhitelist, CONFIG.renderBlacklist, itemEntity.getItem())
                && (!CONFIG.requireGround || itemEntity.isOnGround());
    }

    /**
     * Returns the color from the item's name, rarity, tag, or override.
     */
    public static Integer getItemColor(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (LootBeams.CRASH_BLACKLIST.contains(itemStack)) {
            return WHITE;
        }

        try {
            // From custom colors
            Integer override = CONFIG.customColors.get(itemStack.getItem());
            if (override != null) {
                return override;
            }

            // From NBT
            CompoundTag tag = itemStack.getTag();
            if (tag != null && tag.contains("lootbeams.color", Tag.TAG_STRING)) {
                return Integer.decode(tag.getString("lootbeams.color"));
            }

            return CONFIG.beamColorMode.getColor(itemEntity, itemStack);
        } catch (Exception e) {
            LootBeams.LOGGER.error("Failed to get color for ({}), added to temporary blacklist", itemStack.getDisplayName());
            LootBeams.LOGGER.error("Error: ", e);
            LootBeams.CRASH_BLACKLIST.add(itemStack);
            LootBeams.LOGGER.info("Temporary blacklist is now : ");
            for (ItemStack stack : LootBeams.CRASH_BLACKLIST) {
                LootBeams.LOGGER.info(stack.getDisplayName());
            }
            return WHITE;
        }
    }

    /**
     * Gets color from the first letter in the text component.
     */
    public static int getRawColor(Component text) {
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
                return color.getValue();
            }
        }

        return WHITE;
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

    public static void cache(ItemEntity itemEntity) {
        nameCache(itemEntity, itemEntity.getItem());
        tooltipCache(itemEntity, itemEntity.getItem());
    }

    public static Component nameCache(ItemEntity itemEntity, ItemStack itemStack) {
        return NAME_CACHE.computeIfAbsent(itemEntity, ie -> itemStack.getHoverName());
    }

    public static List<Component> tooltipCache(ItemEntity itemEntity, ItemStack itemStack) {
        return TOOLTIP_CACHE.computeIfAbsent(itemEntity, ie -> itemStack.getTooltipLines(null, TooltipFlag.Default.NORMAL));
    }

    public static void unCache(ItemEntity itemEntity) {
        NAME_CACHE.remove(itemEntity);
        TOOLTIP_CACHE.remove(itemEntity);
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

    public static int color(float r, float g, float b, float a) {
        return color((int) r * 255, (int) g * 255, (int) b * 255, (int) a * 255);
    }

    public static int color(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                (b & 0xFF);
    }

    public static float r(int color) {
        return ((color >> 16) & 0xFF) / 255.0f;
    }

    public static float g(int color) {
        return ((color >> 8) & 0xFF) / 255.0f;
    }

    public static float b(int color) {
        return (color & 0xFF) / 255.0f;
    }

    public static float a(int color) {
        return ((color >> 24) & 0xFF) / 255.0f;
    }

    public static boolean passes(ItemCondition condition, List<Item> whitelist, List<Item> blacklist, ItemStack itemStack) {
        return (condition.test(itemStack) || whitelist.contains(itemStack.getItem())) && !blacklist.contains(itemStack.getItem());
    }

    public static int color(int color, float a) {
        return color(r(color), g(color), b(color), a);
    }
}
