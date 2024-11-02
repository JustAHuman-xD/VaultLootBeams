package me.justahuman.vaultlootbeams.utils;

import me.justahuman.vaultlootbeams.VaultLootBeams;
import me.justahuman.vaultlootbeams.api.LootBeamHolder;
import me.justahuman.vaultlootbeams.client.types.ItemCondition;
import me.justahuman.vaultlootbeams.client.types.ItemList;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static me.justahuman.vaultlootbeams.config.ModConfig.CONFIG;

public class Utils {
    private static final Map<ItemEntity, List<Component>> TOOLTIP_CACHE = new ConcurrentHashMap<>();
    private static boolean warnings = true;

    public static boolean rendersBeam(ItemEntity itemEntity) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.distanceToSqr(itemEntity) > Math.pow(CONFIG.renderDistance, 2) || (CONFIG.requireGround && !itemEntity.isOnGround())) {
            return false;
        }

        ItemStack itemStack = itemEntity.getItem();
        if (itemStack.getItem() instanceof LootBeamHolder holder) {
            return holder.shouldRenderBeam(itemEntity, itemStack);
        } else if (CONFIG.whitelistColorOverrides && CONFIG.colorOverrides.contains(itemStack.getItem())) {
            return true;
        }
        return passes(CONFIG.renderCondition, CONFIG.renderWhitelist, CONFIG.renderBlacklist, itemEntity.getItem());
    }

    public static Color getGradientColor(ItemEntity itemEntity, List<Color> colors) {
        if (colors.size() == 1) {
            return colors.get(0);
        }

        int stage = (itemEntity.getAge() / 30) % colors.size();
        int progress = itemEntity.getAge() % 30;

        Color colorStart = colors.get(stage);
        Color colorEnd = colors.get((stage + 1) % colors.size());
        float blendFactor = progress / 30.0f;
        return blendColors(colorStart, colorEnd, blendFactor);
    }

    /**
     * Returns the color from the item's name, rarity, tag, or override.
     */
    public static Color getItemColor(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (VaultLootBeams.CRASH_BLACKLIST.contains(itemStack)) {
            return Color.WHITE;
        }

        // From player config
        List<Color> override = CONFIG.colorOverrides.get(itemStack.getItem());
        if (override != null && !override.isEmpty()) {
            return getGradientColor(itemEntity, override);
        }

        // From Item
        if (itemStack.getItem() instanceof LootBeamHolder holder) {
            return holder.getBeamColor(itemEntity, itemStack);
        }

        // From NBT
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains("lootbeams.color", Tag.TAG_STRING)) {
            try {
                return Color.decode(tag.getString("lootbeams.color"));
            } catch (Exception e) {
                VaultLootBeams.LOGGER.error("Failed to get color for ({}), added to temporary blacklist", itemStack.getDisplayName());
                VaultLootBeams.LOGGER.error("Error: ", e);
                VaultLootBeams.CRASH_BLACKLIST.add(itemStack);
                VaultLootBeams.LOGGER.info("Temporary blacklist is now : ");
                for (ItemStack stack : VaultLootBeams.CRASH_BLACKLIST) {
                    VaultLootBeams.LOGGER.info(stack.getDisplayName());
                }
                return Color.WHITE;
            }
        }

        // Client Config Color Mode
        return CONFIG.beamColorMode.getColor(itemEntity, itemStack);
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

    public static void cache(ItemEntity itemEntity) {
        tooltipCache(itemEntity, itemEntity.getItem());
    }

    public static List<Component> tooltipCache(ItemEntity itemEntity, ItemStack itemStack) {
        return TOOLTIP_CACHE.computeIfAbsent(itemEntity, ie -> itemStack.getTooltipLines(null, TooltipFlag.Default.NORMAL));
    }

    public static void unCache(ItemEntity itemEntity) {
        TOOLTIP_CACHE.remove(itemEntity);
    }

    public static boolean isModId(String context, String itemKey) {
        if (!ModList.get().isLoaded(itemKey)) {
            if (warnings) {
                VaultLootBeams.LOGGER.warn("Couldn't find mod for id \"{}\" in {}", itemKey, context);
            }
            return false;
        }
        return true;
    }

    public static TagKey<Item> getTag(String context, String itemKey) {
        // skip the # before the tag identifier
        ResourceLocation tagResource = ResourceLocation.tryParse(itemKey.substring(1));
        if (tagResource == null) {
            if (warnings) {
                VaultLootBeams.LOGGER.warn("Invalid tag identifier \"{}\" in {}", itemKey, context);
            }
            return null;
        }

        ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        if (tagManager == null) {
            if (warnings) {
                VaultLootBeams.LOGGER.warn("Couldn't find tag manager for items, something has gone very wrong");
            }
            return null;
        }

        boolean found = tagManager.getTagNames()
                .map(TagKey::location)
                .anyMatch(tagResource::equals);
        if (!found) {
            if (warnings) {
                VaultLootBeams.LOGGER.warn("Couldn't find tag for identifier \"{}\" in {}", itemKey, context);
            }
            return null;
        }
        return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), tagResource);
    }

    public static Item getItem(String context, String itemKey) {
        ResourceLocation itemResource = ResourceLocation.tryParse(itemKey);
        if (itemResource == null) {
            if (warnings) {
                VaultLootBeams.LOGGER.warn("Invalid item identifier \"{}\" in {}", itemKey, context);
            }
            return null;
        }

        Item item = ForgeRegistries.ITEMS.getValue(itemResource);
        if (item == null || item == Items.AIR) {
            if (warnings) {
                VaultLootBeams.LOGGER.warn("Couldn't find item for identifier \"{}\" in {}", itemKey, context);
            }
            return null;
        }
        return item;
    }

    public static Color color(Color color, float a) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (a * 255));
    }

    private static Color blendColors(Color color1, Color color2, float ratio) {
        float iRatio = 1 - ratio;
        return new Color(
                (int) (color1.getRed() * iRatio + color2.getRed() * ratio),
                (int) (color1.getGreen() * iRatio + color2.getGreen() * ratio),
                (int) (color1.getBlue() * iRatio + color2.getBlue() * ratio)
        );
    }

    public static boolean passes(ItemCondition condition, ItemList whitelist, ItemList blacklist, ItemStack itemStack) {
        Item item = itemStack.getItem();
        return (condition.test(itemStack) || whitelist.contains(item)) && !blacklist.contains(item);
    }

    public static void enableWarnings() {
        warnings = true;
    }

    public static void disableWarnings() {
        warnings = false;
    }
}
