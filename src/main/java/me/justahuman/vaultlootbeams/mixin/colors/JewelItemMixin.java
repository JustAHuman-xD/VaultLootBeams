package me.justahuman.vaultlootbeams.mixin.colors;

import iskallia.vault.item.tool.JewelItem;
import me.justahuman.vaultlootbeams.api.LootBeamHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.awt.*;

@Mixin(JewelItem.class)
public class JewelItemMixin implements LootBeamHolder {
    @Override
    public Color getBeamColor(ItemEntity entity, ItemStack itemStack) {
        return new Color(JewelItem.getColor(itemStack));
    }
}
