package me.justahuman.vaultlootbeams.mixin.colors.vault;

import iskallia.vault.item.GodBlessingItem;
import me.justahuman.vaultlootbeams.api.LootBeamHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;
import java.awt.*;

@Mixin(GodBlessingItem.class)
public class BlessingItemMixin implements LootBeamHolder {
    @Override
    public @Nonnull Color getBeamColor(ItemEntity entity, ItemStack itemStack) {
        return new Color(GodBlessingItem.getGod(itemStack).getColor());
    }
}
