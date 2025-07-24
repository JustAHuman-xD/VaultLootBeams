package me.justahuman.vaultlootbeams.mixin.colors.vault;

import iskallia.vault.item.InfusedCatalystItem;
import me.justahuman.vaultlootbeams.api.LootBeamHolder;
import me.justahuman.vaultlootbeams.utils.Utils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;
import java.awt.*;

import static me.justahuman.vaultlootbeams.utils.ItemColors.*;

@Mixin(InfusedCatalystItem.class)
public class InfusedCatalystItemMixin implements LootBeamHolder {
    @Override
    public @Nonnull Color getBeamColor(ItemEntity entity, ItemStack itemStack) {
        int model = itemStack.getTag() == null ? 0 : itemStack.getTag().getInt("model");
        return Utils.getGradientColor(entity, switch(model) {
            case 1 -> WOODEN;
            case 2, 10 -> GILDED;
            case 3, 11 -> LIVING;
            case 4, 12 -> ORNATE;
            case 5, 13 -> WEALTHY;
            case 6 -> EXTENDED;
            case 7 -> ACCUSTOMED;
            case 8 -> PLENTIFUL;
            case 9 -> SOUL_BOOST;
            default -> DEFAULT;
        });
    }
}
