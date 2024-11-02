package me.justahuman.vaultlootbeams.mixin.colors;

import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.VaultCrystalItem;
import me.justahuman.vaultlootbeams.api.LootBeamHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.awt.*;

@Mixin(VaultCrystalItem.class)
public class CrystalItemMixin implements LootBeamHolder {
    @Override
    public Color getBeamColor(ItemEntity entity, ItemStack itemStack) {
        CrystalData data = CrystalData.read(itemStack);
        return new Color(data.getModel().getBlockColor(data, (float) entity.getAge()));
    }
}
