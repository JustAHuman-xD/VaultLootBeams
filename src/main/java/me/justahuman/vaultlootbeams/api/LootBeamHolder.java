package me.justahuman.vaultlootbeams.api;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public interface LootBeamHolder {
    Color getBeamColor(ItemEntity entity, ItemStack itemStack);

    default boolean shouldRenderBeam(ItemEntity entity, ItemStack itemStack) {
        return true;
    }
}
