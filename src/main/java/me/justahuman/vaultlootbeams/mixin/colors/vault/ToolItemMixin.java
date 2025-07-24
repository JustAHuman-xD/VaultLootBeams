package me.justahuman.vaultlootbeams.mixin.colors.vault;

import iskallia.vault.item.tool.ToolItem;
import me.justahuman.vaultlootbeams.api.LootBeamHolder;
import me.justahuman.vaultlootbeams.utils.Utils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;
import java.awt.*;

import static me.justahuman.vaultlootbeams.utils.ItemColors.*;

@Mixin(ToolItem.class)
public class ToolItemMixin implements LootBeamHolder {
    @Override
    public @Nonnull Color getBeamColor(ItemEntity entity, ItemStack itemStack) {
        return Utils.getGradientColor(entity, switch(ToolItem.getMaterial(itemStack)) {
            case CHROMATIC_IRON_INGOT -> CHROMATIC_IRON;
            case CHROMATIC_STEEL_INGOT -> CHROMATIC_STEEL;
            case VAULTERITE_INGOT, VAULT_ALLOY -> VAULTERITE;
            case BLACK_CHROMATIC_STEEL_INGOT -> BLACK_CHROMATIC_STEEL;
            case ECHOING_INGOT -> ECHOING;
            case OMEGA_POG -> POG;
            case ROYALE -> ROYALE;
        });
    }
}
