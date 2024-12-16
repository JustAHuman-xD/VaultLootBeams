package me.justahuman.vaultlootbeams.client.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.justahuman.vaultlootbeams.utils.JsonUtils;

import java.util.Map;

public record ParticleGroup(double particleSize, double particleSpeed, double particleSpread, int particleCount,
                            int particleLifetime, ItemCondition particleCondition, ItemList particleWhitelist,
                            ItemList particleBlacklist) {
    public static final ParticleGroup DEFAULT = new ParticleGroup(0.25, 0.1, 0.05, 19, 20,
            ItemCondition.LISTS_ONLY, new ItemList(), new ItemList());

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

    public static Map<String, ParticleGroup> defaultGroups() {
        return Map.of("default", DEFAULT);
    }

    public static ParticleGroup deserialize(JsonObject root) {
        if (root == null) {
            return null;
        }

        double particleSize = JsonUtils.getBounded(root, "particleSize", 0.00001, 10, DEFAULT.particleSize);
        double particleSpeed = JsonUtils.getBounded(root, "particleSpeed", 0.00001, 10, DEFAULT.particleSpeed);
        double particleSpread = JsonUtils.getBounded(root, "particleSpread", 0.00001, 10, DEFAULT.particleSpread);
        int particleCount = JsonUtils.getBounded(root, "particleCount", 1, 20, DEFAULT.particleCount);
        int particleLifetime = JsonUtils.getBounded(root, "particleLifetime", 1, 100, DEFAULT.particleLifetime);
        ItemCondition particleCondition = JsonUtils.get(root, "particleCondition", DEFAULT.particleCondition, ItemCondition.class);
        ItemList particleWhitelist = ItemList.deserialize(root, "particleWhitelist", DEFAULT.particleWhitelist);
        ItemList particleBlacklist = ItemList.deserialize(root, "particleBlacklist", DEFAULT.particleBlacklist);
        return new ParticleGroup(particleSize, particleSpeed, particleSpread, particleCount, particleLifetime, particleCondition, particleWhitelist, particleBlacklist);
    }
}
