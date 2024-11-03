package me.justahuman.vaultlootbeams.compat;

import net.minecraftforge.fml.ModList;

public class ModCompat {
    public static void init() {
        ModList modList = ModList.get();
        if (modList.isLoaded("the_vault")) {
            VaultCompat.init();
        }
    }
}
