package com.lootbeams.config.types;

import com.lootbeams.utils.Utils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;

public enum BeamColorMode {
    DEFAULT((itemEntity, itemStack) -> Utils.WHITE),
    NAME_COLOR((itemEntity, itemStack) -> Utils.getRawColor(Utils.nameCache(itemEntity, itemStack))),
    RARITY_COLOR((itemEntity, itemStack) -> itemStack.getRarity().color.getColor()),
    NAME_OR_RARITY_COLOR((itemEntity, itemStack) -> {
        int nameColor = Utils.getRawColor(Utils.nameCache(itemEntity, itemStack));
        return nameColor != Utils.WHITE ? Integer.valueOf(nameColor) : itemStack.getRarity().color.getColor();
    });

    private final BiFunction<ItemEntity, ItemStack, Integer> colorFunction;

    BeamColorMode(BiFunction<ItemEntity, ItemStack, Integer> colorFunction) {
        this.colorFunction = colorFunction;
    }

    public int getColor(ItemEntity itemEntity, ItemStack itemStack) {
        return colorFunction.apply(itemEntity, itemStack);
    }
}
