package com.lootbeams.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lootbeams.LootBeams;
import com.lootbeams.config.types.BeamColorMode;
import com.lootbeams.config.types.BeamRenderMode;
import com.lootbeams.config.types.ItemCondition;
import com.lootbeams.utils.JsonUtils;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ModConfig {
    private static final Gson GSON = new Gson().newBuilder().setPrettyPrinting().create();
    public static final ModConfig DEFAULT = new ModConfig();
    public static final ModConfig CONFIG = new ModConfig();

    public double renderDistance = 24;
    public boolean requireGround = true;
    public ItemCondition renderCondition = ItemCondition.LISTS_ONLY;
    public List<Item> renderWhitelist = List.of();
    public List<Item> renderBlacklist = List.of();

    public double beamRadius = 1;
    public double beamHeight = 2;
    public double beamYOffset = 0;
    public double beamAlpha = 0.5;
    public boolean whiteBeamCenter = true;
    public boolean beamShadow = true;
    public double shadowRadius = 0.5;
    public boolean animateShadow = true;
    public BeamRenderMode beamRenderMode = BeamRenderMode.GLOWING;
    public BeamColorMode beamColorMode = BeamColorMode.NAME_OR_RARITY_COLOR;
    public Map<Item, Integer> customColors = Map.of();

    public boolean beamParticles = true;
    public double particleSize = 0.25;
    public double particleSpeed = 0.1;
    public double particleSpread = 0.05;
    public int particleCount = 19;
    public int particleLifetime = 20;
    public ItemCondition particleCondition = ItemCondition.LISTS_ONLY;
    public List<Item> particleWhitelist = List.of();
    public List<Item> particleBlacklist = List.of();

    public boolean beamNameplate = true;
    public boolean nameplateOnLook = true;
    public double nameplateLookSensitivity = 0.018;
    public boolean nameplateOutline = true;
    public boolean nameplateIncludeCount = true;
    public double nameplateScale = 1;
    public double nameplateYOffset = 0.75;
    public double nameplateTextAlpha = 1;
    public double nameplateBackgroundAlpha = 0.5;
    public boolean renderVanillaRarities = false;
    public List<String> customNameplateRarities = List.of();
    public ItemCondition nameplateCondition = ItemCondition.LISTS_ONLY;
    public List<Item> nameplateWhitelist = List.of();
    public List<Item> nameplateBlacklist = List.of();

    public boolean landingSound = false;
    public double soundVolume = 0.3;
    public ItemCondition soundCondition = ItemCondition.LISTS_ONLY;
    public List<Item> soundWhitelist = List.of();
    public List<Item> soundBlacklist = List.of();

    public void loadFromFile() {
        final JsonObject root = new JsonObject();
        try (final FileReader reader = new FileReader(getConfigFile())) {
            if (JsonParser.parseReader(reader) instanceof JsonObject jsonObject) {
                jsonObject.entrySet().forEach(entry -> root.add(entry.getKey(), entry.getValue()));
            }
        } catch (Exception e) {
            LootBeams.LOGGER.error("Error loading the config!", e);
        }

        renderDistance = JsonUtils.getBounded(root, "renderDistance", 0, 1024, DEFAULT.renderDistance);
        requireGround = JsonUtils.get(root, "requireGround", DEFAULT.requireGround);
        renderCondition = JsonUtils.get(root, "renderCondition", DEFAULT.renderCondition);
        renderWhitelist = JsonUtils.getItemList(root, "renderWhitelist", DEFAULT.renderWhitelist);
        renderBlacklist = JsonUtils.getItemList(root, "renderBlacklist", DEFAULT.renderBlacklist);

        beamRadius = JsonUtils.getBounded(root, "beamRadius", 0, 5, DEFAULT.beamRadius);
        beamHeight = JsonUtils.getBounded(root, "beamHeight", 0, 10, DEFAULT.beamHeight);
        beamYOffset = JsonUtils.getBounded(root, "beamYOffset", -30, 30, DEFAULT.beamYOffset);
        beamAlpha = JsonUtils.getBounded(root, "beamAlpha", 0, 1, DEFAULT.beamAlpha);
        whiteBeamCenter = JsonUtils.get(root, "whiteBeamCenter", DEFAULT.whiteBeamCenter);
        beamShadow = JsonUtils.get(root, "beamShadow", DEFAULT.beamShadow);
        shadowRadius = JsonUtils.getBounded(root, "shadowRadius", 0.00001, 1, DEFAULT.shadowRadius);
        animateShadow = JsonUtils.get(root, "animateShadow", DEFAULT.animateShadow);
        beamRenderMode = JsonUtils.get(root, "beamRenderMode", DEFAULT.beamRenderMode);
        beamColorMode = JsonUtils.get(root, "beamColorMode", DEFAULT.beamColorMode);
        customColors = JsonUtils.loadCustomColors(root, "customColors", DEFAULT.customColors);

        beamParticles = JsonUtils.get(root, "beamParticles", DEFAULT.beamParticles);
        particleSize = JsonUtils.getBounded(root, "particleSize", 0.00001, 10, DEFAULT.particleSize);
        particleSpeed = JsonUtils.getBounded(root, "particleSpeed", 0.00001, 10, DEFAULT.particleSpeed);
        particleSpread = JsonUtils.getBounded(root, "particleSpread", 0.00001, 10, DEFAULT.particleSpread);
        particleCount = JsonUtils.getBounded(root, "particleCount", 1, 20, DEFAULT.particleCount);
        particleLifetime = JsonUtils.getBounded(root, "particleLifetime", 1, 100, DEFAULT.particleLifetime);
        particleCondition = JsonUtils.get(root, "particleCondition", DEFAULT.particleCondition);
        particleWhitelist = JsonUtils.getItemList(root, "particleWhitelist", DEFAULT.particleWhitelist);
        particleBlacklist = JsonUtils.getItemList(root, "particleBlacklist", DEFAULT.particleBlacklist);

        beamNameplate = JsonUtils.get(root, "beamNameplate", DEFAULT.beamNameplate);
        nameplateOnLook = JsonUtils.get(root, "nameplateOnLook", DEFAULT.nameplateOnLook);
        nameplateLookSensitivity = JsonUtils.getBounded(root, "nameplateLookSensitivity", 0, 5, DEFAULT.nameplateLookSensitivity);
        nameplateOutline = JsonUtils.get(root, "nameplateOutline", DEFAULT.nameplateOutline);
        nameplateIncludeCount = JsonUtils.get(root, "nameplateIncludeCount", DEFAULT.nameplateIncludeCount);
        nameplateScale = JsonUtils.getBounded(root, "nameplateScale", -10, 10, DEFAULT.nameplateScale);
        nameplateYOffset = JsonUtils.getBounded(root, "nameplateYOffset", -30, 30, DEFAULT.nameplateYOffset);
        nameplateTextAlpha = JsonUtils.getBounded(root, "nameplateTextAlpha", 0, 1, DEFAULT.nameplateTextAlpha);
        nameplateBackgroundAlpha = JsonUtils.getBounded(root, "nameplateBackgroundAlpha", 0, 1, DEFAULT.nameplateBackgroundAlpha);
        renderVanillaRarities = JsonUtils.get(root, "renderVanillaRarities", DEFAULT.renderVanillaRarities);
        customNameplateRarities = JsonUtils.getList(root, "customNameplateRarities", DEFAULT.customNameplateRarities);
        nameplateCondition = JsonUtils.get(root, "nameplateCondition", DEFAULT.nameplateCondition);
        nameplateWhitelist = JsonUtils.getItemList(root, "nameplateWhitelist", DEFAULT.nameplateWhitelist);
        nameplateBlacklist = JsonUtils.getItemList(root, "nameplateBlacklist", DEFAULT.nameplateBlacklist);

        landingSound = JsonUtils.get(root, "landingSound", DEFAULT.landingSound);
        soundVolume = JsonUtils.getBounded(root, "soundVolume", 0, 1, DEFAULT.soundVolume);
        soundCondition = JsonUtils.get(root, "soundCondition", DEFAULT.soundCondition);
        soundWhitelist = JsonUtils.getItemList(root, "soundWhitelist", DEFAULT.soundWhitelist);
        soundBlacklist = JsonUtils.getItemList(root, "soundBlacklist", DEFAULT.soundBlacklist);
    }

    public void saveToFile() {
        JsonObject root = new JsonObject();
        root.addProperty("renderDistance", renderDistance);
        root.addProperty("requireGround", requireGround);
        root.addProperty("renderCondition", renderCondition.name());
        root.add("renderWhitelist", JsonUtils.serializeItemList(renderWhitelist));
        root.add("renderBlacklist", JsonUtils.serializeItemList(renderBlacklist));

        root.addProperty("beamRadius", beamRadius);
        root.addProperty("beamHeight", beamHeight);
        root.addProperty("beamYOffset", beamYOffset);
        root.addProperty("beamAlpha", beamAlpha);
        root.addProperty("whiteBeamCenter", whiteBeamCenter);
        root.addProperty("beamShadow", beamShadow);
        root.addProperty("shadowRadius", shadowRadius);
        root.addProperty("animateShadow", animateShadow);
        root.addProperty("beamRenderMode", beamRenderMode.name());
        root.addProperty("beamColorMode", beamColorMode.name());
        root.add("customColors", JsonUtils.serializeCustomColors(customColors));

        root.addProperty("beamParticles", beamParticles);
        root.addProperty("particleSize", particleSize);
        root.addProperty("particleSpeed", particleSpeed);
        root.addProperty("particleSpread", particleSpread);
        root.addProperty("particleCount", particleCount);
        root.addProperty("particleLifetime", particleLifetime);
        root.addProperty("particleCondition", particleCondition.name());
        root.add("particleWhitelist", JsonUtils.serializeItemList(particleWhitelist));
        root.add("particleBlacklist", JsonUtils.serializeItemList(particleBlacklist));

        root.addProperty("beamNameplate", beamNameplate);
        root.addProperty("nameplateOnLook", nameplateOnLook);
        root.addProperty("nameplateLookSensitivity", nameplateLookSensitivity);
        root.addProperty("nameplateOutline", nameplateOutline);
        root.addProperty("nameplateIncludeCount", nameplateIncludeCount);
        root.addProperty("nameplateScale", nameplateScale);
        root.addProperty("nameplateYOffset", nameplateYOffset);
        root.addProperty("nameplateTextAlpha", nameplateTextAlpha);
        root.addProperty("nameplateBackgroundAlpha", nameplateBackgroundAlpha);
        root.addProperty("renderVanillaRarities", renderVanillaRarities);
        root.add("customNameplateRarities", JsonUtils.serializeList(customNameplateRarities));
        root.addProperty("nameplateCondition", nameplateCondition.name());
        root.add("nameplateWhitelist", JsonUtils.serializeItemList(nameplateWhitelist));
        root.add("nameplateBlacklist", JsonUtils.serializeItemList(nameplateBlacklist));

        root.addProperty("landingSound", landingSound);
        root.addProperty("soundVolume", soundVolume);
        root.addProperty("soundCondition", soundCondition.name());
        root.add("soundWhitelist", JsonUtils.serializeItemList(soundWhitelist));
        root.add("soundBlacklist", JsonUtils.serializeItemList(soundBlacklist));

        try (final FileWriter fileWriter = new FileWriter(getConfigFile())) {
            GSON.toJson(root, fileWriter);
            fileWriter.flush();
        } catch (IOException e) {
            LootBeams.LOGGER.error("Error saving the config!", e);
        }
    }

    private static File getConfigFile() {
        File configFile = new File(FMLPaths.CONFIGDIR.get().toFile(), "lootbeams.json");
        try {
            if (configFile.exists() && configFile.isDirectory()) {
                if (!configFile.delete()) {
                    LootBeams.LOGGER.error("Failed to delete invalid config file");
                } else {
                    LootBeams.LOGGER.info("Deleted invalid config file (was a directory)");
                }
            }
        } catch (Exception e) {
            LootBeams.LOGGER.error("Failed to delete invalid config file", e);
        }

        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                if (!configFile.createNewFile()) {
                    LootBeams.LOGGER.error("Failed to create config file");
                } else {
                    LootBeams.LOGGER.info("Created config file");
                }
            } catch (Exception e) {
                LootBeams.LOGGER.error("Failed to create config file", e);
            }
        }

        return configFile;
    }
}
