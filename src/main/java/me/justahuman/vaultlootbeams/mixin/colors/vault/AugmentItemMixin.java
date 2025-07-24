package me.justahuman.vaultlootbeams.mixin.colors.vault;

import iskallia.vault.item.AugmentItem;
import me.justahuman.vaultlootbeams.api.LootBeamHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;
import java.awt.*;

@Mixin(AugmentItem.class)
public class AugmentItemMixin implements LootBeamHolder {
    @Override
    public @Nonnull Color getBeamColor(ItemEntity entity, ItemStack itemStack) {
        return new Color(AugmentItem.getColor(itemStack));
    }
}
