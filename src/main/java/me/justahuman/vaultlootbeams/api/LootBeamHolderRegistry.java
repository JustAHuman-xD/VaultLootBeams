package me.justahuman.vaultlootbeams.api;

import me.justahuman.vaultlootbeams.VaultLootBeams;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LootBeamHolderRegistry {
    private static final StackWalker STACK_WALKER;
    static {
        StackWalker walker = null;
        try {
            walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        } catch (Throwable e) {
            VaultLootBeams.LOGGER.error("Failed to initialize StackWalker, will not remember LootBeamHolderRegistry sources", e);
        }
        STACK_WALKER = walker;
    }

    private static final Map<Item, Class<?>> SOURCES = new HashMap<>();

    /**
     * Registers a LootBeamHolder for the given item.
     *
     * @param item   The item to register the holder for.
     * @param holder The LootBeamHolder to register or null to remove the holder.
     */
    public static void registerHolder(@Nonnull Item item, @Nullable LootBeamHolder holder) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        Class<?> source = null;
        if (STACK_WALKER != null) {
            try {
                source = STACK_WALKER.getCallerClass();
            } catch (Throwable e) {
                VaultLootBeams.LOGGER.error("Failed to get caller class for LootBeamHolder registration", e);
            }
        }

        if (((ItemExtension) item).hasLootBeamHolder()) {
            Class<?> originalSource = SOURCES.get(item);
            if (originalSource != null) {
                VaultLootBeams.LOGGER.warn("LootBeamHolder registered for {} by {} is being overridden by {}", item, originalSource.getName(), source != null ? source.getName() : "Unknown Source");
            } else {
                VaultLootBeams.LOGGER.warn("Item {} is already registered. Overriding with new holder from {}", item, source != null ? source.getName() : "Unknown Source");
            }
        }
        ((ItemExtension) item).vaultLootBeams$setLootBeamHolder(holder);
        SOURCES.put(item, source);
    }
}
