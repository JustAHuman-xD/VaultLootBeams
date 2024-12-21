package me.justahuman.vaultlootbeams.client.config;

import me.justahuman.vaultlootbeams.VaultLootBeams;
import me.justahuman.vaultlootbeams.client.types.BeamColorMode;
import me.justahuman.vaultlootbeams.client.types.BeamRenderMode;
import me.justahuman.vaultlootbeams.client.types.ColorMap;
import me.justahuman.vaultlootbeams.client.types.ItemCondition;
import me.justahuman.vaultlootbeams.client.types.ItemList;
import me.justahuman.vaultlootbeams.client.types.ParticleGroup;
import me.justahuman.vaultlootbeams.utils.Utils;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static me.justahuman.vaultlootbeams.client.config.ModConfig.CONFIG;
import static me.justahuman.vaultlootbeams.client.config.ModConfig.DEFAULT;

public class ConfigScreen {
    public static Screen create() {
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(translate("title"))
                .setParentScreen(null)
                .setSavingRunnable(CONFIG::saveToFile);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(translate("category.general"));
        ConfigCategory beamProperties = builder.getOrCreateCategory(translate("category.beam_properties"));
        ConfigCategory beamColors = builder.getOrCreateCategory(translate("category.beam_colors"));
        ConfigCategory beamParticles = builder.getOrCreateCategory(translate("category.beam_particles"));
        ConfigCategory beamNameplate = builder.getOrCreateCategory(translate("category.beam_nameplate"));
        ConfigCategory landingSound = builder.getOrCreateCategory(translate("category.landing_sound"));

        general.addEntry(doubleEntry(entryBuilder, "renderDistance", 0, 1024));
        general.addEntry(booleanEntry(entryBuilder, "requireGround"));
        general.addEntry(enumEntry(entryBuilder, "renderCondition", ItemCondition.class));
        general.addEntry(itemListEntry(entryBuilder, "renderWhitelist"));
        general.addEntry(itemListEntry(entryBuilder, "renderBlacklist"));
        general.addEntry(entryBuilder.startTextDescription(translate("listSyntax")).build());

        beamProperties.addEntry(doubleEntry(entryBuilder, "beamRadius", 0, 5));
        beamProperties.addEntry(doubleEntry(entryBuilder, "beamHeight", 0, 10));
        beamProperties.addEntry(doubleEntry(entryBuilder, "beamYOffset", -30, 30));
        beamProperties.addEntry(doubleEntry(entryBuilder, "beamAlpha", 0.00001, 1));
        beamProperties.addEntry(booleanEntry(entryBuilder, "whiteBeamCenter"));
        beamProperties.addEntry(booleanEntry(entryBuilder, "beamShadow"));
        beamProperties.addEntry(doubleEntry(entryBuilder, "shadowRadius", 0.00001, 1));
        beamProperties.addEntry(doubleEntry(entryBuilder, "shadowAlphaMultiplier", 0.00001, 3));
        beamProperties.addEntry(booleanEntry(entryBuilder, "animateShadow"));
        beamProperties.addEntry(enumEntry(entryBuilder, "beamRenderMode", BeamRenderMode.class));

        beamColors.addEntry(enumEntry(entryBuilder, "beamColorMode", BeamColorMode.class));
        List<String> colorGroups = CONFIG.colorOverrides.colors.stream().map(group -> group.id).toList();
        beamColors.addEntry(entryBuilder.startStrList(translate("manage_color_groups"), colorGroups)
                .setTooltip(translate("manage_color_groups.tooltip"))
                .setDefaultValue(colorGroups)
                .setSaveConsumer(newValue -> {
                    CONFIG.colorOverrides.colors.removeIf(group -> !newValue.contains(group.id));
                    newValue.removeAll(CONFIG.colorOverrides.colors.stream().map(group -> group.id).toList());
                    newValue.forEach(id -> CONFIG.colorOverrides.colors.add(new ColorMap.ColorGroup(id, new ItemList(), List.of())));
                    CONFIG.colorOverrides.cache.clear();
                }).build());
        for (ColorMap.ColorGroup group : CONFIG.colorOverrides.colors) {
            List<String> colors = group.colors.stream().map(ColorMap::serialize).toList();
            SubCategoryBuilder colorCategory = entryBuilder.startSubCategory(new TextComponent(group.id));
            colorCategory.add(entryBuilder.startStrField(translate("new_id"), group.id)
                    .setTooltip(translate("new_id.tooltip"))
                    .setDefaultValue(group.id)
                    .setSaveConsumer(newValue -> group.id = newValue)
                    .build());
            colorCategory.add(itemListEntry(entryBuilder, "items", group, group, ColorMap.ColorGroup.class));
            colorCategory.add(entryBuilder.startStrList(translate("colors"), colors)
                    .setCellErrorSupplier(string -> {
                        try {
                            Color.decode(string);
                            return Optional.empty();
                        } catch (Exception e) {
                            return Optional.of(translate("error.invalidColor"));
                        }
                    })
                    .setTooltipSupplier(newValue -> {
                        if (newValue.isEmpty()) {
                            return Optional.of(new Component[] { translate("colors.tooltip") });
                        }
                        List<Component> tooltip = new ArrayList<>();
                        tooltip.add(translate("colors.preview"));
                        for (String color : newValue) {
                            try {
                                tooltip.add(new TextComponent(color).withStyle(style -> style.withColor(Color.decode(color).getRGB())));
                            } catch (Exception ignored) {}
                        }
                        tooltip.add(translate("colors.tooltip"));
                        return Optional.of(tooltip.toArray(new Component[0]));
                    })
                    .setDefaultValue(colors)
                    .setSaveConsumer(newValue -> {
                        group.colors.clear();
                        for (String color : newValue) {
                            try {
                                group.colors.add(Color.decode(color));
                            } catch (Exception e) {
                                VaultLootBeams.LOGGER.warn("Invalid color \"{}\" for group \"{}\" in {}", color, group.id, "beamColors");
                            }
                        }
                    }).build());
            beamColors.addEntry(colorCategory.build());
        }

        beamParticles.addEntry(booleanEntry(entryBuilder, "beamParticles"));
        List<String> particleGroups = CONFIG.particleGroups.keySet().stream().toList();
        beamParticles.addEntry(entryBuilder.startStrList(translate("manage_particle_groups"), particleGroups)
                .setTooltip(translate("manage_particle_groups.tooltip"))
                .setDefaultValue(particleGroups)
                .setSaveConsumer(newValue -> {
                    Map<String, ParticleGroup> newGroups = new HashMap<>();
                    newValue.forEach(group -> newGroups.put(group, CONFIG.particleGroups.getOrDefault(group, ParticleGroup.defaultGroup())));
                    CONFIG.particleGroups = newGroups;
                }).build());
        for (Map.Entry<String, ParticleGroup> groupEntry : CONFIG.particleGroups.entrySet()) {
            String id = groupEntry.getKey();
            ParticleGroup group = groupEntry.getValue();
            SubCategoryBuilder groupCategory = entryBuilder.startSubCategory(new TextComponent(id));
            groupCategory.add(entryBuilder.startStrField(translate("new_id"), id)
                    .setTooltip(translate("new_id.tooltip"))
                    .setDefaultValue(id)
                    .setSaveConsumer(newValue -> {
                        Map<String, ParticleGroup> newGroups = new HashMap<>();
                        CONFIG.particleGroups.forEach((key, value) -> newGroups.put(key.equals(id) ? newValue : key, value));
                        CONFIG.particleGroups = newGroups;
                    }).build());
            groupCategory.add(doubleEntry(entryBuilder, "particleSize", 0.00001, 10, group, group, ParticleGroup.class));
            groupCategory.add(doubleEntry(entryBuilder, "particleSpeed", 0.00001, 10, group, group, ParticleGroup.class));
            groupCategory.add(doubleEntry(entryBuilder, "particleSpread", 0.00001, 10, group, group, ParticleGroup.class));
            groupCategory.add(intEntry(entryBuilder, "particleCount", 1, 20, group, group, ParticleGroup.class));
            groupCategory.add(intEntry(entryBuilder, "particleLifetime", 1, 100, group, group, ParticleGroup.class));
            groupCategory.add(enumEntry(entryBuilder, "particleCondition", ItemCondition.class, group, group, ParticleGroup.class));
            groupCategory.add(itemListEntry(entryBuilder, "particleWhitelist", group, group, ParticleGroup.class));
            groupCategory.add(itemListEntry(entryBuilder, "particleBlacklist", group, group, ParticleGroup.class));
            beamParticles.addEntry(groupCategory.build());
        }

        beamNameplate.addEntry(booleanEntry(entryBuilder, "beamNameplate"));
        beamNameplate.addEntry(booleanEntry(entryBuilder, "nameplateOnLook"));
        beamNameplate.addEntry(doubleEntry(entryBuilder, "nameplateLookSensitivity", 0, 5));
        beamNameplate.addEntry(booleanEntry(entryBuilder, "nameplateOutline"));
        beamNameplate.addEntry(booleanEntry(entryBuilder, "nameplateIncludeCount"));
        beamNameplate.addEntry(doubleEntry(entryBuilder, "nameplateScale", -10, 10));
        beamNameplate.addEntry(doubleEntry(entryBuilder, "nameplateYOffset", -30, 30));
        beamNameplate.addEntry(doubleEntry(entryBuilder, "nameplateTextAlpha", 0, 1));
        beamNameplate.addEntry(doubleEntry(entryBuilder, "nameplateBackgroundAlpha", 0, 1));
        beamNameplate.addEntry(booleanEntry(entryBuilder, "renderVanillaRarities"));
        beamNameplate.addEntry(stringListEntry(entryBuilder, "customNameplateRarities"));
        beamNameplate.addEntry(enumEntry(entryBuilder, "nameplateCondition", ItemCondition.class));
        beamNameplate.addEntry(itemListEntry(entryBuilder, "nameplateWhitelist"));
        beamNameplate.addEntry(itemListEntry(entryBuilder, "nameplateBlacklist"));

        landingSound.addEntry(booleanEntry(entryBuilder, "landingSound"));
        landingSound.addEntry(doubleEntry(entryBuilder, "soundVolume", 0, 1));
        landingSound.addEntry(enumEntry(entryBuilder, "soundCondition", ItemCondition.class));
        landingSound.addEntry(itemListEntry(entryBuilder, "soundWhitelist"));
        landingSound.addEntry(itemListEntry(entryBuilder, "soundBlacklist"));
        return builder.build();
    }

    public static AbstractConfigListEntry<?> booleanEntry(ConfigEntryBuilder entryBuilder, String fieldName) {
        return booleanEntry(entryBuilder, fieldName, CONFIG, DEFAULT, ModConfig.class);
    }

    public static <C> AbstractConfigListEntry<?> booleanEntry(ConfigEntryBuilder entryBuilder, String fieldName, C config, C defConfig, Class<C> configClass) {
        try {
            Field field = configClass.getDeclaredField(fieldName);
            boolean value = field.getBoolean(config);
            boolean def = field.getBoolean(defConfig);
            return entryBuilder.startBooleanToggle(translate(fieldName), value)
                    .setTooltip(translate(fieldName + ".tooltip"))
                    .setDefaultValue(def)
                    .setSaveConsumer(newValue -> attemptSet(field, newValue, fieldName, newValue, config))
                    .build();
        } catch (Exception e) {
            logFailedEntry(fieldName);
            return null;
        }
    }

    public static AbstractConfigListEntry<?> intEntry(ConfigEntryBuilder entryBuilder, String fieldName, int min, int max) {
        return intEntry(entryBuilder, fieldName, min, max, CONFIG, DEFAULT, ModConfig.class);
    }

    public static <C> AbstractConfigListEntry<?> intEntry(ConfigEntryBuilder entryBuilder, String fieldName, int min, int max, C config, C defConfig, Class<C> configClass) {
        try {
            Field field = configClass.getDeclaredField(fieldName);
            int value = field.getInt(config);
            int def = field.getInt(defConfig);
            return entryBuilder.startIntSlider(translate(fieldName), value, min, max)
                    .setTooltip(translate(fieldName + ".tooltip")).setDefaultValue(def)
                    .setSaveConsumer(newValue -> attemptSet(field, newValue, fieldName, newValue, config))
                    .build();
        } catch (Exception e) {
            logFailedEntry(fieldName);
            return null;
        }
    }

    public static AbstractConfigListEntry<?> doubleEntry(ConfigEntryBuilder entryBuilder, String fieldName, double min, double max) {
        return doubleEntry(entryBuilder, fieldName, min, max, CONFIG, DEFAULT, ModConfig.class);
    }

    public static <C> AbstractConfigListEntry<?> doubleEntry(ConfigEntryBuilder entryBuilder, String fieldName, double min, double max, C config, C defConfig, Class<C> configClass) {
        try {
            Field field = configClass.getDeclaredField(fieldName);
            double value = field.getDouble(config);
            double def = field.getDouble(defConfig);
            return entryBuilder.startDoubleField(translate(fieldName), value)
                    .setTooltip(translate(fieldName + ".tooltip"))
                    .setMin(min).setMax(max).setDefaultValue(def)
                    .setSaveConsumer(newValue -> attemptSet(field, newValue, fieldName, newValue, config))
                    .build();
        } catch (Exception e) {
            logFailedEntry(fieldName);
            return null;
        }
    }

    public static <E extends Enum<E>> AbstractConfigListEntry<?> enumEntry(ConfigEntryBuilder entryBuilder, String fieldName, Class<E> enumClass) {
        return enumEntry(entryBuilder, fieldName, enumClass, CONFIG, DEFAULT, ModConfig.class);
    }

    public static <C, E extends Enum<E>> AbstractConfigListEntry<?> enumEntry(ConfigEntryBuilder entryBuilder, String fieldName, Class<E> enumClass, C config, C defConfig, Class<C> configClass) {
        try {
            Field field = configClass.getDeclaredField(fieldName);
            E value = (E) field.get(config);
            E def = (E) field.get(defConfig);
            return entryBuilder.startEnumSelector(translate(fieldName), enumClass, value)
                    .setTooltip(translate(fieldName + ".tooltip"))
                    .setDefaultValue(def)
                    .setSaveConsumer(newValue -> attemptSet(field, newValue, fieldName, newValue, config))
                    .build();
        } catch (Exception e) {
            logFailedEntry(fieldName);
            return null;
        }
    }

    public static AbstractConfigListEntry<?> stringListEntry(ConfigEntryBuilder entryBuilder, String fieldName) {
        return stringListEntry(entryBuilder, fieldName, CONFIG, DEFAULT, ModConfig.class);
    }

    public static <C> AbstractConfigListEntry<?> stringListEntry(ConfigEntryBuilder entryBuilder, String fieldName, C config, C defConfig, Class<C> configClass) {
        try {
            Field field = configClass.getDeclaredField(fieldName);
            List<String> value = (List<String>) field.get(config);
            List<String> def = (List<String>) field.get(defConfig);
            return entryBuilder.startStrList(translate(fieldName), value)
                    .setTooltip(translate(fieldName + ".tooltip"))
                    .setDefaultValue(def)
                    .setSaveConsumer(newValue -> attemptSet(field, newValue, fieldName, newValue, config))
                    .build();
        } catch (Exception e) {
            logFailedEntry(fieldName);
            return null;
        }
    }

    public static AbstractConfigListEntry<?> itemListEntry(ConfigEntryBuilder entryBuilder, String fieldName) {
        return itemListEntry(entryBuilder, fieldName, CONFIG, DEFAULT, ModConfig.class);
    }

    public static <C> AbstractConfigListEntry<?> itemListEntry(ConfigEntryBuilder entryBuilder, String fieldName, C config, C defConfig, Class<C> configClass) {
        try {
            Field field = configClass.getDeclaredField(fieldName);
            ItemList value = (ItemList) field.get(config);
            ItemList def = (ItemList) field.get(defConfig);
            return entryBuilder.startStrList(translate(fieldName), value.toStringList())
                    .setCellErrorSupplier(string -> {
                        Utils.disableWarnings();
                        if (string.startsWith("#")) {
                            if (Utils.getTag(fieldName, string) == null) {
                                return Optional.of(translate("error.invalidTag"));
                            }
                        } else if (!string.contains(":")) {
                            if (!Utils.isModId(fieldName, string)) {
                                return Optional.of(translate("error.invalidModId"));
                            }
                        } else if (string.contains("*")) {
                            if (Utils.getMatchingItems(fieldName, string).isEmpty()) {
                                return Optional.of(translate("error.invalidItemMatcher"));
                            }
                        } else {
                            if (Utils.getItem(fieldName, string) == null) {
                                return Optional.of(translate("error.invalidItem"));
                            }
                        }
                        return Optional.empty();
                    })
                    .setTooltip(translate(fieldName + ".tooltip"))
                    .setDefaultValue(def.toStringList())
                    .setSaveConsumer(newValue -> attemptSet(field, ItemList.deserialize(fieldName, newValue), fieldName, newValue, config))
                    .build();
        } catch (Exception e) {
            logFailedEntry(fieldName);
            return null;
        }
    }

    private static void attemptSet(Field field, Object value, String fieldName, Object newValue, Object config) {
        try {
            field.set(config, value);
        } catch (Exception e) {
            VaultLootBeams.LOGGER.error("Failed to set config entry {} to \"{}\"", fieldName, newValue);
        }
    }

    private static void logFailedEntry(String fieldName) {
        VaultLootBeams.LOGGER.error("Failed to add config entry for field {}", fieldName);
    }

    public static Component translate(String key) {
        return new TranslatableComponent(VaultLootBeams.MODID + ".config." + key);
    }
}
