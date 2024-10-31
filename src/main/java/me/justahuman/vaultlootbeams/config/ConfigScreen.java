package me.justahuman.vaultlootbeams.config;

import me.justahuman.vaultlootbeams.VaultLootBeams;
import me.justahuman.vaultlootbeams.client.types.BeamColorMode;
import me.justahuman.vaultlootbeams.client.types.BeamRenderMode;
import me.justahuman.vaultlootbeams.client.types.ItemCondition;
import me.justahuman.vaultlootbeams.client.types.ItemList;
import me.justahuman.vaultlootbeams.client.types.temp.CullShard;
import me.justahuman.vaultlootbeams.client.types.temp.DepthShard;
import me.justahuman.vaultlootbeams.client.types.temp.LayeringShard;
import me.justahuman.vaultlootbeams.client.types.temp.LightmapShard;
import me.justahuman.vaultlootbeams.client.types.temp.LineShard;
import me.justahuman.vaultlootbeams.client.types.temp.OutputShard;
import me.justahuman.vaultlootbeams.client.types.temp.OverlayShard;
import me.justahuman.vaultlootbeams.client.types.temp.ShaderShard;
import me.justahuman.vaultlootbeams.client.types.temp.TextureShard;
import me.justahuman.vaultlootbeams.client.types.temp.TexturingShard;
import me.justahuman.vaultlootbeams.client.types.temp.TransparencyShard;
import me.justahuman.vaultlootbeams.client.types.temp.WriteShard;
import me.justahuman.vaultlootbeams.utils.Utils;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static me.justahuman.vaultlootbeams.config.ModConfig.CONFIG;
import static me.justahuman.vaultlootbeams.config.ModConfig.DEFAULT;

public class ConfigScreen {
    public static Screen create() {
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(translate("title"))
                .setParentScreen(null)
                .setSavingRunnable(CONFIG::saveToFile);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory renderType = builder.getOrCreateCategory(translate("category.render_type"));
        ConfigCategory general = builder.getOrCreateCategory(translate("category.general"));
        ConfigCategory beamProperties = builder.getOrCreateCategory(translate("category.beam_properties"));
        ConfigCategory beamParticles = builder.getOrCreateCategory(translate("category.beam_particles"));
        ConfigCategory beamNameplate = builder.getOrCreateCategory(translate("category.beam_nameplate"));
        ConfigCategory landingSound = builder.getOrCreateCategory(translate("category.landing_sound"));

        enumEntry(renderType, entryBuilder, "cullShard", CullShard.class);
        enumEntry(renderType, entryBuilder, "depthShard", DepthShard.class);
        enumEntry(renderType, entryBuilder, "layeringShard", LayeringShard.class);
        enumEntry(renderType, entryBuilder, "lightmapShard", LightmapShard.class);
        enumEntry(renderType, entryBuilder, "lineShard", LineShard.class);
        enumEntry(renderType, entryBuilder, "outputShard", OutputShard.class);
        enumEntry(renderType, entryBuilder, "overlayShard", OverlayShard.class);
        enumEntry(renderType, entryBuilder, "shaderShard", ShaderShard.class);
        enumEntry(renderType, entryBuilder, "textureShard", TextureShard.class);
        enumEntry(renderType, entryBuilder, "texturingShard", TexturingShard.class);
        enumEntry(renderType, entryBuilder, "transparencyShard", TransparencyShard.class);
        enumEntry(renderType, entryBuilder, "writeShard", WriteShard.class);
        booleanEntry(renderType, entryBuilder, "affectsOutline");

        doubleEntry(general, entryBuilder, "renderDistance", 0, 1024);
        booleanEntry(general, entryBuilder, "requireGround");
        enumEntry(general, entryBuilder, "renderCondition", ItemCondition.class);
        itemListEntry(general, entryBuilder, "renderWhitelist");
        itemListEntry(general, entryBuilder, "renderBlacklist");
        general.addEntry(entryBuilder.startTextDescription(translate("listSyntax")).build());

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
        beamProperties.addEntry(entryBuilder.startTextDescription(translate("colorOverrides")).build());

        booleanEntry(beamParticles, entryBuilder, "beamParticles");
        doubleEntry(beamParticles, entryBuilder, "particleSize", 0.00001, 10);
        doubleEntry(beamParticles, entryBuilder, "particleSpeed", 0.00001, 10);
        doubleEntry(beamParticles, entryBuilder, "particleSpread", 0.00001, 10);
        intEntry(beamParticles, entryBuilder, "particleCount", 1, 20);
        intEntry(beamParticles, entryBuilder, "particleLifetime", 1, 100);
        enumEntry(beamParticles, entryBuilder, "particleCondition", ItemCondition.class);
        itemListEntry(beamParticles, entryBuilder, "particleWhitelist");
        itemListEntry(beamParticles, entryBuilder, "particleBlacklist");

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
        stringListEntry(beamNameplate, entryBuilder, "customNameplateRarities");
        enumEntry(beamNameplate, entryBuilder, "nameplateCondition", ItemCondition.class);
        itemListEntry(beamNameplate, entryBuilder, "nameplateWhitelist");
        itemListEntry(beamNameplate, entryBuilder, "nameplateBlacklist");

        booleanEntry(landingSound, entryBuilder, "landingSound");
        doubleEntry(landingSound, entryBuilder, "soundVolume", 0, 1);
        enumEntry(landingSound, entryBuilder, "soundCondition", ItemCondition.class);
        itemListEntry(landingSound, entryBuilder, "soundWhitelist");
        itemListEntry(landingSound, entryBuilder, "soundBlacklist");
        return builder.build();
    }

    public static void booleanEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, String fieldName) {
        try {
            Field field = ModConfig.class.getDeclaredField(fieldName);
            boolean value = field.getBoolean(CONFIG);
            boolean def = field.getBoolean(DEFAULT);
            category.addEntry(entryBuilder.startBooleanToggle(translate(fieldName), value)
                    .setTooltip(translate(fieldName + ".tooltip"))
                    .setDefaultValue(def)
                    .setSaveConsumer(newValue -> attemptSet(field, newValue, fieldName, newValue))
                    .build());
        } catch (Exception e) {
            logFailedEntry(fieldName);
        }
    }

    public static void intEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, String fieldName, int min, int max) {
        try {
            Field field = ModConfig.class.getDeclaredField(fieldName);
            int value = field.getInt(CONFIG);
            int def = field.getInt(DEFAULT);
            category.addEntry(entryBuilder.startIntSlider(translate(fieldName), value, min, max)
                    .setTooltip(translate(fieldName + ".tooltip")).setDefaultValue(def)
                    .setSaveConsumer(newValue -> attemptSet(field, newValue, fieldName, newValue))
                    .build());
        } catch (Exception e) {
            logFailedEntry(fieldName);
        }
    }

    public static void doubleEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, String fieldName, double min, double max) {
        try {
            Field field = ModConfig.class.getDeclaredField(fieldName);
            double value = field.getDouble(CONFIG);
            double def = field.getDouble(DEFAULT);
            category.addEntry(entryBuilder.startDoubleField(translate(fieldName), value)
                    .setTooltip(translate(fieldName + ".tooltip"))
                    .setMin(min).setMax(max).setDefaultValue(def)
                    .setSaveConsumer(newValue -> attemptSet(field, newValue, fieldName, newValue))
                    .build());
        } catch (Exception e) {
            logFailedEntry(fieldName);
        }
    }

    public static <E extends Enum<E>> void enumEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, String fieldName, Class<E> enumClass) {
        try {
            Field field = ModConfig.class.getDeclaredField(fieldName);
            E value = (E) field.get(CONFIG);
            E def = (E) field.get(DEFAULT);
            category.addEntry(entryBuilder.startEnumSelector(translate(fieldName), enumClass, value)
                    .setTooltip(translate(fieldName + ".tooltip"))
                    .setDefaultValue(def)
                    .setSaveConsumer(newValue -> attemptSet(field, newValue, fieldName, newValue))
                    .build());
        } catch (Exception e) {
            logFailedEntry(fieldName);
        }
    }

    public static void stringListEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, String fieldName) {
        try {
            Field field = ModConfig.class.getDeclaredField(fieldName);
            List<String> value = (List<String>) field.get(CONFIG);
            List<String> def = (List<String>) field.get(DEFAULT);
            category.addEntry(entryBuilder.startStrList(translate(fieldName), value)
                    .setTooltip(translate(fieldName + ".tooltip"))
                    .setDefaultValue(def)
                    .setSaveConsumer(newValue -> attemptSet(field, newValue, fieldName, newValue))
                    .build());
        } catch (Exception e) {
            logFailedEntry(fieldName);
        }
    }

    public static void itemListEntry(ConfigCategory category, ConfigEntryBuilder entryBuilder, String fieldName) {
        try {
            Field field = ModConfig.class.getDeclaredField(fieldName);
            ItemList value = (ItemList) field.get(CONFIG);
            ItemList def = (ItemList) field.get(DEFAULT);
            category.addEntry(entryBuilder.startStrList(translate(fieldName), value.toStringList())
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
                        } else {
                            if (Utils.getItem(fieldName, string) == null) {
                                return Optional.of(translate("error.invalidItem"));
                            }
                        }
                        return Optional.empty();
                    })
                    .setTooltip(translate(fieldName + ".tooltip"))
                    .setDefaultValue(def.toStringList())
                    .setSaveConsumer(newValue -> attemptSet(field, ItemList.deserialize(fieldName, newValue), fieldName, newValue))
                    .build());
        } catch (Exception e) {
            logFailedEntry(fieldName);
        }
    }

    private static void attemptSet(Field field, Object value, String fieldName, Object newValue) {
        try {
            field.set(CONFIG, value);
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
