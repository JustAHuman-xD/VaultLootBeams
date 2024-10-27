package com.lootbeams.config.types;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;

import java.util.function.Predicate;

public enum ItemCondition {
    LISTS_ONLY(itemStack -> false),
    ALL_ITEMS(itemStack -> true),
    EQUIPMENT(itemStack -> {
        Item item = itemStack.getItem();
        return item instanceof TieredItem
                || item instanceof ArmorItem
                || item instanceof ShieldItem
                || item instanceof BowItem
                || item instanceof CrossbowItem;
    }),
    RARE_ITEMS(itemStack -> itemStack.getRarity() != Rarity.COMMON);

    private final Predicate<ItemStack> predicate;

    ItemCondition(Predicate<ItemStack> predicate) {
        this.predicate = predicate;
    }

    public boolean test(ItemStack itemStack) {
        return predicate.test(itemStack);
    }
}
