package com.lootbeams.utils;

import com.lootbeams.LootBeams;
import com.lootbeams.config.types.ItemCondition;
import com.lootbeams.config.types.ItemList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

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
            List<Integer> override = CONFIG.customColors.get(itemStack.getItem());
            if (override != null && !override.isEmpty()) {
                if (override.size() == 1) {
                    return override.get(0);
                }

                int stage = (itemEntity.getAge() / 40) % override.size();
                int progress = itemEntity.getAge() % 40;

                int colorStart = override.get(stage);
                int colorEnd = override.get((stage + 1) % override.size());
                float blendFactor = progress / 40.0f;
                return blendColors(colorStart, colorEnd, blendFactor);
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

    public static boolean isModId(String context, String itemKey) {
        if (!ModList.get().isLoaded(itemKey)) {
            LootBeams.LOGGER.warn("Couldn't find mod for id \"{}\" in {}", itemKey, context);
            return false;
        }
        return true;
    }

    public static TagKey<Item> getTag(String context, String itemKey) {
        // skip the # before the tag identifier
        ResourceLocation tagResource = ResourceLocation.tryParse(itemKey.substring(1));
        if (tagResource == null) {
            LootBeams.LOGGER.warn("Invalid tag identifier \"{}\" in {}", itemKey, context);
            return null;
        }

        ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        if (tagManager == null) {
            LootBeams.LOGGER.warn("Couldn't find tag manager for items, something has gone very wrong");
            return null;
        }

        boolean found = tagManager.getTagNames()
                .map(TagKey::location)
                .anyMatch(tagResource::equals);
        if (!found) {
            LootBeams.LOGGER.warn("Couldn't find tag for identifier \"{}\" in {}", itemKey, context);
            return null;
        }
        return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), tagResource);
    }

    public static Item getItem(String context, String itemKey) {
        ResourceLocation itemResource = ResourceLocation.tryParse(itemKey);
        if (itemResource == null) {
            LootBeams.LOGGER.warn("Invalid item identifier \"{}\" in {}", itemKey, context);
            return null;
        }

        Item item = ForgeRegistries.ITEMS.getValue(itemResource);
        if (item == null) {
            LootBeams.LOGGER.warn("Couldn't find item for identifier \"{}\" in {}", itemKey, context);
            return null;
        }
        return item;
    }

    public static int color(int color, float a) {
        return color(rC(color), gC(color), bC(color), a);
    }

    public static int color(float r, float g, float b) {
        return color(r, g, b, 1);
    }

    public static int color(float r, float g, float b, float a) {
        return color((int) r * 255, (int) g * 255, (int) b * 255, (int) a * 255);
    }

    public static int color(int r, int g, int b) {
        return color(r, g, b, 255);
    }

    public static int color(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                (b & 0xFF);
    }

    public static float r(int color) {
        return (color >> 16) & 0xFF;
    }

    public static float rC(int color) {
        return r(color) / 255.0f;
    }

    public static float g(int color) {
        return (color >> 8) & 0xFF;
    }

    public static float gC(int color) {
        return g(color) / 255.0f;
    }

    public static int b(int color) {
        return color & 0xFF;
    }

    public static float bC(int color) {
        return b(color) / 255.0f;
    }

    public static int a(int color) {
        return (color >> 24) & 0xFF;
    }

    public static float aC(int color) {
        return a(color) / 255.0f;
    }

    private static int blendColors(int color1, int color2, float ratio) {
        float iRatio = 1 - ratio;
        return color((int) ((r(color1) * iRatio) + r(color2) * ratio),
                (int) (g(color1) * iRatio + g(color2) * ratio),
                (int) (b(color1) * iRatio + b(color2) * ratio),
                (int) (a(color1) * iRatio + a(color2) * ratio));
    }

    public static boolean passes(ItemCondition condition, ItemList whitelist, ItemList blacklist, ItemStack itemStack) {
        return (condition.test(itemStack) || whitelist.contains(itemStack.getItem())) && !blacklist.contains(itemStack.getItem());
    }
}
