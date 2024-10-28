package me.justahuman.vaultlootbeams.client.types;

import me.justahuman.vaultlootbeams.utils.Utils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.function.BiFunction;

public enum BeamColorMode {
    DEFAULT((itemEntity, itemStack) -> Color.WHITE),
    NAME_COLOR((itemEntity, itemStack) -> Utils.getRawColor(itemStack.getHoverName())),
    RARITY_COLOR((itemEntity, itemStack) -> new Color(itemStack.getRarity().color.getColor())),
    NAME_OR_RARITY((itemEntity, itemStack) -> {
        Color nameColor = NAME_COLOR.getColor(itemEntity, itemStack);
        return nameColor != Color.WHITE ? nameColor : RARITY_COLOR.getColor(itemEntity, itemStack);
    });

    private final BiFunction<ItemEntity, ItemStack, Color> colorFunction;

    BeamColorMode(BiFunction<ItemEntity, ItemStack, Color> colorFunction) {
        this.colorFunction = colorFunction;
    }

    public Color getColor(ItemEntity itemEntity, ItemStack itemStack) {
        return colorFunction.apply(itemEntity, itemStack);
    }
}
