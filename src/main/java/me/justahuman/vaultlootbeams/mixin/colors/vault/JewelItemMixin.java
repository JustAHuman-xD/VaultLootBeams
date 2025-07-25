package me.justahuman.vaultlootbeams.mixin.colors.vault;

import iskallia.vault.item.tool.JewelItem;
import me.justahuman.vaultlootbeams.api.LootBeamHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;
import java.awt.*;

@Mixin(JewelItem.class)
public class JewelItemMixin implements LootBeamHolder {
    @Override
    public @Nonnull Color getBeamColor(ItemEntity entity, ItemStack itemStack, float partialTicks) {
        return new Color(JewelItem.getColor(itemStack));
    }
}
