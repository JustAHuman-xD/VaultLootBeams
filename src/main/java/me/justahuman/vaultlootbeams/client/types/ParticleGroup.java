package me.justahuman.vaultlootbeams.client.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.justahuman.vaultlootbeams.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public final class ParticleGroup {
    public static final ParticleGroup DEFAULT = defaultGroup();
    public double particleSize;
    public double particleSpeed;
    public double particleSpread;
    public int particleCount;
    public int particleLifetime;
    public ItemCondition particleCondition;
    public ItemList particleWhitelist;
    public ItemList particleBlacklist;

    public ParticleGroup(double particleSize, double particleSpeed, double particleSpread, int particleCount,
                         int particleLifetime, ItemCondition particleCondition, ItemList particleWhitelist,
                         ItemList particleBlacklist) {
        this.particleSize = particleSize;
        this.particleSpeed = particleSpeed;
        this.particleSpread = particleSpread;
        this.particleCount = particleCount;
        this.particleLifetime = particleLifetime;
        this.particleCondition = particleCondition;
        this.particleWhitelist = particleWhitelist;
        this.particleBlacklist = particleBlacklist;
    }

    public JsonElement serialize() {
        JsonObject root = new JsonObject();
        root.addProperty("particleSize", particleSize);
        root.addProperty("particleSpeed", particleSpeed);
        root.addProperty("particleSpread", particleSpread);
        root.addProperty("particleCount", particleCount);
        root.addProperty("particleLifetime", particleLifetime);
        root.addProperty("particleCondition", particleCondition.name());
        root.add("particleWhitelist", particleWhitelist.serialize());
        root.add("particleBlacklist", particleBlacklist.serialize());
        return root;
    }

    public static ParticleGroup defaultGroup() {
        return new ParticleGroup(
                0.25,
                0.1,
                0.05,
                19,
                20,
                ItemCondition.LISTS_ONLY,
                new ItemList(),
                new ItemList()
        );
    }

    public static Map<String, ParticleGroup> defaultGroups() {
        return new HashMap<>(Map.of("default", defaultGroup()));
    }

    public static ParticleGroup deserialize(JsonObject root) {
        if (root == null) {
            return null;
        }

        ParticleGroup defaultGroup = defaultGroup();
        double particleSize = JsonUtils.getBounded(root, "particleSize", 0.00001, 10, defaultGroup.particleSize);
        double particleSpeed = JsonUtils.getBounded(root, "particleSpeed", 0.00001, 10, defaultGroup.particleSpeed);
        double particleSpread = JsonUtils.getBounded(root, "particleSpread", 0.00001, 10, defaultGroup.particleSpread);
        int particleCount = JsonUtils.getBounded(root, "particleCount", 1, 20, defaultGroup.particleCount);
        int particleLifetime = JsonUtils.getBounded(root, "particleLifetime", 1, 100, defaultGroup.particleLifetime);
        ItemCondition particleCondition = JsonUtils.get(root, "particleCondition", defaultGroup.particleCondition, ItemCondition.class);
        ItemList particleWhitelist = ItemList.deserialize(root, "particleWhitelist", defaultGroup.particleWhitelist);
        ItemList particleBlacklist = ItemList.deserialize(root, "particleBlacklist", defaultGroup.particleBlacklist);
        return new ParticleGroup(particleSize, particleSpeed, particleSpread, particleCount, particleLifetime, particleCondition, particleWhitelist, particleBlacklist);
    }
}
