package com.lootbeams.config;

import com.lootbeams.LootBeams;
import com.lootbeams.config.types.BeamColorMode;
import com.lootbeams.config.types.BeamRenderMode;
import com.lootbeams.config.types.ItemCondition;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.lang.reflect.Field;

import static com.lootbeams.config.ModConfig.CONFIG;
import static com.lootbeams.config.ModConfig.DEFAULT;

public class ConfigScreen {
    public static Screen create() {
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(translate("config.title"))
                .setParentScreen(null)
                .setSavingRunnable(CONFIG::saveToFile);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(translate("config.category.general"));
        ConfigCategory beamProperties = builder.getOrCreateCategory(translate("config.category.beam_properties"));
        ConfigCategory beamParticles = builder.getOrCreateCategory(translate("config.category.beam_particles"));
        ConfigCategory beamNameplate = builder.getOrCreateCategory(translate("config.category.beam_nameplate"));
        ConfigCategory landingSound = builder.getOrCreateCategory(translate("config.category.landing_sound"));

        doubleEntry(general, entryBuilder, "renderDistance", 0, 1024);
        booleanEntry(general, entryBuilder, "requireGround");
        enumEntry(general, entryBuilder, "renderCondition", ItemCondition.class);
        // TODO: renderWhitelist, renderBlacklist

        doubleEntry(beamProperties, entryBuilder, "beamRadius", 0, 5);
        doubleEntry(beamProperties, entryBuilder, "beamHeight", 0, 10);
        doubleEntry(beamProperties, entryBuilder, "beamYOffset", -30, 30);
        doubleEntry(beamProperties, entryBuilder, "beamAlpha", 0, 1);
        booleanEntry(beamProperties, entryBuilder, "whiteBeamCenter");
        booleanEntry(beamProperties, entryBuilder, "beamShadow");
        doubleEntry(beamProperties, entryBuilder, "shadowRadius", 0.00001, 1);
        booleanEntry(beamProperties, entryBuilder, "animateShadow");
        enumEntry(beamProperties, entryBuilder, "beamRenderMode", BeamRenderMode.class);
        enumEntry(beamProperties, entryBuilder, "beamColorMode", BeamColorMode.class);
        // TODO: customColors

        booleanEntry(beamParticles, entryBuilder, "beamParticles");
        doubleEntry(beamParticles, entryBuilder, "particleSize", 0.00001, 10);
        doubleEntry(beamParticles, entryBuilder, "particleSpeed", 0.00001, 10);
        doubleEntry(beamParticles, entryBuilder, "particleSpread", 0.00001, 10);
        intEntry(beamParticles, entryBuilder, "particleCount", 1, 20);
        intEntry(beamParticles, entryBuilder, "particleLifetime", 1, 100);
        enumEntry(beamParticles, entryBuilder, "particleCondition", ItemCondition.class);
        // TODO: particleWhitelist, particleBlacklist

        booleanEntry(beamNameplate, entryBuilder, "beamNameplate");
        booleanEntry(beamNameplate, entryBuilder, "nameplateOnLook");
        doubleEntry(beamNameplate, entryBuilder, "nameplateLookSensitivity", 0, 5);
        booleanEntry(beamNameplate, entryBuilder, "nameplateOutline");
        booleanEntry(beamNameplate, entryBuilder, "nameplateIncludeCount");
        doubleEntry(beamNameplate, entryBuilder, "nameplateScale", -10, 10);
        doubleEntry(beamNameplate, entryBuilder, "nameplateYOffset", -30, 30);
        doubleEntry(beamNameplate, entryBuilder, "nameplateTextAlpha", 0, 1);
        doubleEntry(beamNameplate, entryBuilder, "nameplateBackgroundAlpha", 0, 1);
        booleanEntry(beamNameplate, entryBuilder, "renderVanillaRarities");
        // TODO: customNameplateRarities
        enumEntry(beamNameplate, entryBuilder, "nameplateCondition", ItemCondition.class);
        // TODO: nameplateWhitelist, nameplateBlacklist

        booleanEntry(landingSound, entryBuilder, "landingSound");
        doubleEntry(landingSound, entryBuilder, "soundVolume", 0, 1);
        enumEntry(landingSound, entryBuilder, "soundCondition", ItemCondition.class);
        // TODO: soundWhitelist, soundBlacklist
        return builder.build();
    }

    public static void booleanEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, String fieldName) {
        try {
            Field configField = ModConfig.class.getDeclaredField(fieldName);
            boolean value = configField.getBoolean(CONFIG);
            boolean def = configField.getBoolean(DEFAULT);
            category.addEntry(entryBuilder.startBooleanToggle(translate("config." + fieldName), value)
                    .setTooltip(translate("config." + fieldName + ".tooltip"))
                    .setDefaultValue(def)
                    .setSaveConsumer(newValue -> {
                        try {
                            configField.setBoolean(CONFIG, newValue);
                        } catch (Exception e) {
                            LootBeams.LOGGER.error("Failed to set config entry {} to \"{}\"", fieldName, newValue);
                        }
                    }).build());
        } catch (Exception e) {
            LootBeams.LOGGER.error("Failed to add config entry for field {}", fieldName);
        }
    }

    public static void intEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, String fieldName, int min, int max) {
        try {
            Field configField = ModConfig.class.getDeclaredField(fieldName);
            int value = configField.getInt(CONFIG);
            int def = configField.getInt(DEFAULT);
            category.addEntry(entryBuilder.startIntField(translate("config." + fieldName), value)
                    .setTooltip(translate("config." + fieldName + ".tooltip"))
                    .setMin(min).setMax(max).setDefaultValue(def)
                    .setSaveConsumer(newValue -> {
                        try {
                            configField.setInt(CONFIG, newValue);
                        } catch (Exception e) {
                            LootBeams.LOGGER.error("Failed to set config entry {} to \"{}\"", fieldName, newValue);
                        }
                    }).build());
        } catch (Exception e) {
            LootBeams.LOGGER.error("Failed to add config entry for field {}", fieldName);
        }
    }

    public static void doubleEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, String fieldName, double min, double max) {
        try {
            Field configField = ModConfig.class.getDeclaredField(fieldName);
            double value = configField.getDouble(CONFIG);
            double def = configField.getDouble(DEFAULT);
            category.addEntry(entryBuilder.startDoubleField(translate("config." + fieldName), value)
                    .setTooltip(translate("config." + fieldName + ".tooltip"))
                    .setMin(min).setMax(max).setDefaultValue(def)
                    .setSaveConsumer(newValue -> {
                        try {
                            configField.setDouble(CONFIG, newValue);
                        } catch (Exception e) {
                            LootBeams.LOGGER.error("Failed to set config entry {} to \"{}\"", fieldName, newValue);
                        }
                    }).build());
        } catch (Exception e) {
            LootBeams.LOGGER.error("Failed to add config entry for field {}", fieldName);
        }
    }

    public static <E extends Enum<E>> void enumEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, String fieldName, Class<E> enumClass) {
        try {
            Field configField = ModConfig.class.getDeclaredField(fieldName);
            E value = (E) configField.get(CONFIG);
            E def = (E) configField.get(DEFAULT);
            category.addEntry(entryBuilder.startEnumSelector(translate("config." + fieldName), enumClass, value)
                    .setTooltip(translate("config." + fieldName + ".tooltip"))
                    .setDefaultValue(def)
                    .setSaveConsumer(newValue -> {
                        try {
                            configField.set(CONFIG, newValue);
                        } catch (Exception e) {
                            LootBeams.LOGGER.error("Failed to set config entry {} to \"{}\"", fieldName, newValue);
                        }
                    }).build());
        } catch (Exception e) {
            LootBeams.LOGGER.error("Failed to add config entry for field {}", fieldName);
        }
    }

    public static Component translate(String key) {
        return new TranslatableComponent("lootbeams." + key);
    }
}
