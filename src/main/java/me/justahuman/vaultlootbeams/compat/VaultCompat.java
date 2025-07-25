package me.justahuman.vaultlootbeams.compat;

import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModItems;
import me.justahuman.vaultlootbeams.utils.ItemColors;

import java.awt.*;

import static me.justahuman.vaultlootbeams.client.config.ModConfig.DEFAULT;

public class VaultCompat {
    public static void init() {
        DEFAULT.renderWhitelist
                .add(ModItems.HELMET, ModItems.CHESTPLATE, ModItems.LEGGINGS, ModItems.BOOTS)
                .add(ModItems.WAND, ModItems.FOCUS, ModItems.SHIELD)
                .add(ModItems.IDOL_BENEVOLENT, ModItems.IDOL_MALEVOLENCE, ModItems.IDOL_OMNISCIENT, ModItems.IDOL_TIMEKEEPER)
                .add(ModItems.SMALL_CHARM, ModItems.LARGE_CHARM, ModItems.GRAND_CHARM, ModItems.MAJESTIC_CHARM)
                .add(ModItems.VAULT_GOD_CHARM)
                .add(ModItems.AXE, ModItems.SWORD)
                .add(ModItems.MAGNET, ModItems.TRINKET)
                .add(ModItems.GEMSTONE, ModItems.KEY_PIECE, ModItems.BLANK_KEY, ModItems.UNIDENTIFIED_TREASURE_KEY)
                .add(ModItems.KNOWLEDGE_STAR)
                .add(ModItems.ANTIQUE);

        DEFAULT.colorOverrides
                .add("artifacts", items -> items.add(
                        ModItems.ARTIFACT_FRAGMENT,
                        ModItems.UNIDENTIFIED_ARTIFACT,
                        ModBlocks.VAULT_ARTIFACT.asItem()),
                ItemColors.ARTIFACT)
                .add(ModItems.LOST_BOUNTY, ItemColors.LOST_BOUNTY)
                .add(ModItems.OLD_NOTES, ItemColors.OLD_NOTES)
                .add(ModItems.BOUNTY_PEARL, ItemColors.BOUNTY_PEARL)
                .add(ModItems.VAULT_CATALYST, ItemColors.CATALYST)
                .add("pog", items -> items.add(
                        ModItems.POG,
                        ModItems.ECHO_POG,
                        ModItems.OMEGA_POG)
                , ItemColors.POG.toArray(new Color[0]))
                .add(ModItems.ASHIUM_KEY, ItemColors.ASHIUM)
                .add(ModItems.BOMIGNITE_KEY, ItemColors.BOMIGNITE_RED,
                        ItemColors.TRANSITION,
                        ItemColors.BOMIGNITE_BLUE,
                        ItemColors.TRANSITION)
                .add(ModItems.GORGINITE_KEY, ItemColors.GORGINITE)
                .add(ModItems.ISKALLIUM_KEY, ItemColors.ISKALLIUM)
                .add(ModItems.PETZANITE_KEY, ItemColors.PETZANITE)
                .add(ModItems.SPARKLETINE_KEY, ItemColors.SPARKLETINE)
                .add(ModItems.TUBIUM_KEY, ItemColors.TUBIUM)
                .add(ModItems.UPALINE_KEY, ItemColors.UPALINE)
                .add(ModItems.XENIUM_KEY, ItemColors.XENIUM)
                .add(ModItems.CRYSTAL_SEAL_RAID, ItemColors.DEVASTATOR)
                .add(ModItems.CRYSTAL_SEAL_ARCHITECT, ItemColors.ARCHITECT)
                .add(ModItems.CRYSTAL_SEAL_HUNTER, ItemColors.HUNTER)
                .add(ModItems.CRYSTAL_SEAL_EXECUTIONER, ItemColors.EXECUTIONER)
                .add(ModItems.CRYSTAL_SEAL_SAGE, ItemColors.SAGE)
                .add(ModItems.CRYSTAL_SEAL_SCOUT, ItemColors.SCOUT)
                .add(ModItems.CRYSTAL_SEAL_PROPHET, ItemColors.PROPHET);
    }
}
