package me.justahuman.vaultlootbeams.mixin.colors;

import iskallia.vault.item.InscriptionItem;
import me.justahuman.vaultlootbeams.api.LootBeamHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.awt.*;

@Mixin(InscriptionItem.class)
public class InscriptionItemMixin implements LootBeamHolder {
    @Override
    public Color getBeamColor(ItemEntity entity, ItemStack itemStack) {
        return new Color(InscriptionItem.getColor(itemStack));
    }
}
