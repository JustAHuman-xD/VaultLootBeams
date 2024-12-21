package me.justahuman.vaultlootbeams.client.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.justahuman.vaultlootbeams.VaultLootBeams;
import me.justahuman.vaultlootbeams.client.types.BeamColorMode;
import me.justahuman.vaultlootbeams.client.types.BeamRenderMode;
import me.justahuman.vaultlootbeams.client.types.ItemCondition;
import me.justahuman.vaultlootbeams.client.types.ItemList;
import me.justahuman.vaultlootbeams.client.types.ColorMap;
import me.justahuman.vaultlootbeams.client.types.ParticleGroup;
import me.justahuman.vaultlootbeams.utils.JsonUtils;
import me.justahuman.vaultlootbeams.utils.Utils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModConfig {
    private static final Gson GSON = new Gson().newBuilder().setPrettyPrinting().create();
    public static final ModConfig DEFAULT = new ModConfig();
    public static final ModConfig CONFIG = new ModConfig();

    public double renderDistance = 24;
    public boolean requireGround = true;
    public boolean whitelistColorOverrides = true;
    public ItemCondition renderCondition = ItemCondition.LISTS_ONLY;
    public ItemList renderWhitelist = new ItemList();
    public ItemList renderBlacklist = new ItemList();

    public double beamRadius = 1;
    public double beamHeight = 2;
    public double beamYOffset = 0;
    public double beamAlpha = 0.7;
    public boolean whiteBeamCenter = true;
    public boolean beamShadow = true;
    public double shadowRadius = 0.5;
    public double shadowAlphaMultiplier = 1;
    public boolean animateShadow = true;
    public BeamRenderMode beamRenderMode = BeamRenderMode.GLOWING;
    public BeamColorMode beamColorMode = BeamColorMode.NAME_OR_RARITY;
    public ColorMap colorOverrides = new ColorMap();

    public boolean beamParticles = true;
    public Map<String, ParticleGroup> particleGroups = ParticleGroup.defaultGroups();
    public ParticleGroup particleGroup(ItemStack itemStack) {
        for (ParticleGroup group : particleGroups.values()) {
            if (Utils.passes(group.particleCondition(), group.particleWhitelist(), group.particleBlacklist(), itemStack)) {
                return group;
            }
        }
        return ParticleGroup.DEFAULT;
    }

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
    public ItemList nameplateWhitelist = new ItemList();
    public ItemList nameplateBlacklist = new ItemList();

    public boolean landingSound = false;
    public double soundVolume = 0.3;
    public ItemCondition soundCondition = ItemCondition.LISTS_ONLY;
    public ItemList soundWhitelist = new ItemList();
    public ItemList soundBlacklist = new ItemList();

    public void loadFromFile() {
        final JsonObject root = new JsonObject();
        try (final FileReader reader = new FileReader(getConfigFile())) {
            if (JsonParser.parseReader(reader) instanceof JsonObject jsonObject) {
                jsonObject.entrySet().forEach(entry -> root.add(entry.getKey(), entry.getValue()));
            }
        } catch (Exception e) {
            VaultLootBeams.LOGGER.error("Error loading the config!", e);
        }

        renderDistance = JsonUtils.getBounded(root, "renderDistance", 0, 1024, DEFAULT.renderDistance);
        requireGround = JsonUtils.get(root, "requireGround", DEFAULT.requireGround);
        renderCondition = JsonUtils.get(root, "renderCondition", DEFAULT.renderCondition, ItemCondition.class);
        renderWhitelist = ItemList.deserialize(root, "renderWhitelist", DEFAULT.renderWhitelist);
        renderBlacklist = ItemList.deserialize(root, "renderBlacklist", DEFAULT.renderBlacklist);

        beamRadius = JsonUtils.getBounded(root, "beamRadius", 0, 5, DEFAULT.beamRadius);
        beamHeight = JsonUtils.getBounded(root, "beamHeight", 0, 10, DEFAULT.beamHeight);
        beamYOffset = JsonUtils.getBounded(root, "beamYOffset", -30, 30, DEFAULT.beamYOffset);
        beamAlpha = JsonUtils.getBounded(root, "beamAlpha", 0, 1, DEFAULT.beamAlpha);
        whiteBeamCenter = JsonUtils.get(root, "whiteBeamCenter", DEFAULT.whiteBeamCenter);
        beamShadow = JsonUtils.get(root, "beamShadow", DEFAULT.beamShadow);
        shadowRadius = JsonUtils.getBounded(root, "shadowRadius", 0.00001, 1, DEFAULT.shadowRadius);
        shadowAlphaMultiplier = JsonUtils.getBounded(root, "shadowAlphaMultiplier", 0.00001, 3, DEFAULT.shadowAlphaMultiplier);
        animateShadow = JsonUtils.get(root, "animateShadow", DEFAULT.animateShadow);
        beamRenderMode = JsonUtils.get(root, "beamRenderMode", DEFAULT.beamRenderMode, BeamRenderMode.class);
        beamColorMode = JsonUtils.get(root, "beamColorMode", DEFAULT.beamColorMode, BeamColorMode.class);
        colorOverrides = ColorMap.deserialize(root, "colorOverrides", DEFAULT.colorOverrides);

        beamParticles = JsonUtils.get(root, "beamParticles", DEFAULT.beamParticles);
        JsonObject serializedGroups = JsonUtils.get(root, "particleGroups", (JsonObject) null);
        if (serializedGroups != null) {
            particleGroups = new HashMap<>();
            for (String group : serializedGroups.keySet()) {
                JsonObject serialized = JsonUtils.get(serializedGroups, group, (JsonObject) null);
                ParticleGroup particleGroup = ParticleGroup.deserialize(serialized);
                if (particleGroup != null) {
                    particleGroups.put(group, particleGroup);
                }
            }
            if (!particleGroups.containsKey("default")) {
                particleGroups.put("default", ParticleGroup.DEFAULT);
            }
        }

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
        nameplateCondition = JsonUtils.get(root, "nameplateCondition", DEFAULT.nameplateCondition, ItemCondition.class);
        nameplateWhitelist = ItemList.deserialize(root, "nameplateWhitelist", DEFAULT.nameplateWhitelist);
        nameplateBlacklist = ItemList.deserialize(root, "nameplateBlacklist", DEFAULT.nameplateBlacklist);

        landingSound = JsonUtils.get(root, "landingSound", DEFAULT.landingSound);
        soundVolume = JsonUtils.getBounded(root, "soundVolume", 0, 1, DEFAULT.soundVolume);
        soundCondition = JsonUtils.get(root, "soundCondition", DEFAULT.soundCondition, ItemCondition.class);
        soundWhitelist = ItemList.deserialize(root, "soundWhitelist", DEFAULT.soundWhitelist);
        soundBlacklist = ItemList.deserialize(root, "soundBlacklist", DEFAULT.soundBlacklist);
    }

    public void saveToFile() {
        JsonObject root = new JsonObject();
        root.addProperty("renderDistance", renderDistance);
        root.addProperty("requireGround", requireGround);
        root.addProperty("renderCondition", renderCondition.name());
        root.add("renderWhitelist", renderWhitelist.serialize());
        root.add("renderBlacklist", renderBlacklist.serialize());

        root.addProperty("beamRadius", beamRadius);
        root.addProperty("beamHeight", beamHeight);
        root.addProperty("beamYOffset", beamYOffset);
        root.addProperty("beamAlpha", beamAlpha);
        root.addProperty("whiteBeamCenter", whiteBeamCenter);
        root.addProperty("beamShadow", beamShadow);
        root.addProperty("shadowRadius", shadowRadius);
        root.addProperty("shadowAlphaMultiplier", shadowAlphaMultiplier);
        root.addProperty("animateShadow", animateShadow);
        root.addProperty("beamRenderMode", beamRenderMode.name());
        root.addProperty("beamColorMode", beamColorMode.name());
        root.add("colorOverrides", colorOverrides.serialize());

        root.addProperty("beamParticles", beamParticles);
        if (!particleGroups.isEmpty()) {
            JsonObject groups = new JsonObject();
            for (Map.Entry<String, ParticleGroup> group : particleGroups.entrySet()) {
                groups.add(group.getKey(), group.getValue().serialize());
            }
            root.add("particleGroups", groups);
        }

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
        root.add("nameplateWhitelist", nameplateWhitelist.serialize());
        root.add("nameplateBlacklist", nameplateBlacklist.serialize());

        root.addProperty("landingSound", landingSound);
        root.addProperty("soundVolume", soundVolume);
        root.addProperty("soundCondition", soundCondition.name());
        root.add("soundWhitelist", soundWhitelist.serialize());
        root.add("soundBlacklist", soundBlacklist.serialize());

        try (final FileWriter fileWriter = new FileWriter(getConfigFile())) {
            GSON.toJson(root, fileWriter);
            fileWriter.flush();
        } catch (IOException e) {
            VaultLootBeams.LOGGER.error("Error saving the config!", e);
        }
    }

    private static File getConfigFile() {
        File configFile = new File(FMLPaths.CONFIGDIR.get().toFile(), VaultLootBeams.MODID + ".json");
        try {
            if (configFile.exists() && configFile.isDirectory()) {
                if (!configFile.delete()) {
                    VaultLootBeams.LOGGER.error("Failed to delete invalid config file");
                } else {
                    VaultLootBeams.LOGGER.info("Deleted invalid config file (was a directory)");
                }
            }
        } catch (Exception e) {
            VaultLootBeams.LOGGER.error("Failed to delete invalid config file", e);
        }

        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                if (!configFile.createNewFile()) {
                    VaultLootBeams.LOGGER.error("Failed to create config file");
                } else {
                    VaultLootBeams.LOGGER.info("Created config file");
                }
            } catch (Exception e) {
                VaultLootBeams.LOGGER.error("Failed to create config file", e);
            }
        }

        return configFile;
    }
}
