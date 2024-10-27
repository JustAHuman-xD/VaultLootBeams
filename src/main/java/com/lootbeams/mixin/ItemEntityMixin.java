package com.lootbeams.mixin;

import com.lootbeams.LootBeams;
import com.lootbeams.utils.Utils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.lootbeams.config.ModConfig.CONFIG;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Unique private boolean lootbeams$hasPlayedSound = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!CONFIG.landingSound) {
            return;
        }

        ItemEntity itemEntity = (ItemEntity) (Object) this;
        if (!lootbeams$hasPlayedSound && (itemEntity.isOnGround() || (itemEntity.isOnGround() && (itemEntity.tickCount < 10 && itemEntity.tickCount > 3)))) {
            if (Utils.passes(CONFIG.soundCondition, CONFIG.soundWhitelist, CONFIG.soundBlacklist, itemEntity.getItem())) {
                itemEntity.level.playSound(null, itemEntity, LootBeams.LOOT_DROP.get(), SoundSource.AMBIENT, (float) CONFIG.soundVolume, ((itemEntity.level.random.nextFloat() - itemEntity.level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }
            lootbeams$hasPlayedSound = true;
        }

        if(lootbeams$hasPlayedSound && !itemEntity.isOnGround()){
            lootbeams$hasPlayedSound = false;
        }
    }
}
