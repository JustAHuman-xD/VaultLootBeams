package me.justahuman.vaultlootbeams.api;

public interface ItemExtension {
    void vaultLootBeams$setLootBeamHolder(LootBeamHolder holder);
    LootBeamHolder vaultLootBeams$getLootBeamHolder();

    default boolean hasLootBeamHolder() {
        return vaultLootBeams$getLootBeamHolder() != null;
    }
}
