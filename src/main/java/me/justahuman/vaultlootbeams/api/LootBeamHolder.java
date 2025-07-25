package me.justahuman.vaultlootbeams.api;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.awt.*;

public interface LootBeamHolder {
    @Nonnull Color getBeamColor(ItemEntity entity, ItemStack itemStack, float partialTicks);

    default boolean shouldRenderBeam(ItemEntity entity, ItemStack itemStack) {
        return true;
    }

    LootBeamHolder NONE = new LootBeamHolder() {
        @Override
        public @Nonnull Color getBeamColor(ItemEntity entity, ItemStack itemStack, float partialTicks) {
            return Color.WHITE;
        }

        @Override
        public boolean shouldRenderBeam(ItemEntity entity, ItemStack itemStack) {
            return false;
        }
    };
}
