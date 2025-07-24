package me.justahuman.vaultlootbeams.mixin;

import me.justahuman.vaultlootbeams.api.ItemExtension;
import me.justahuman.vaultlootbeams.api.LootBeamHolder;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

import static me.justahuman.vaultlootbeams.api.LootBeamHolder.NONE;

@Mixin(Item.class)
public class ItemMixin implements ItemExtension {
    @Unique
    private LootBeamHolder vaultLootBeams$lootBeamHolder = NONE;

    @Override
    public void vaultLootBeams$setLootBeamHolder(LootBeamHolder holder) {
        vaultLootBeams$lootBeamHolder = holder;
    }

    @Override
    public @Nullable LootBeamHolder vaultLootBeams$getLootBeamHolder() {
        if (vaultLootBeams$lootBeamHolder == NONE) {
            if (this instanceof LootBeamHolder holder) {
                vaultLootBeams$lootBeamHolder = holder;
            } else {
                return null;
            }
        }
        return vaultLootBeams$lootBeamHolder;
    }
}
