package com.lootbeams.mixin;

import com.lootbeams.ClientSetup;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Unique
    private boolean lootbeams$hasPlayedSound = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        if (!lootbeams$hasPlayedSound && (itemEntity.isOnGround() || (itemEntity.isOnGround() && (itemEntity.tickCount < 10 && itemEntity.tickCount > 3)))) {
            ClientSetup.playDropSound(itemEntity);
            lootbeams$hasPlayedSound = true;
        }

        if(lootbeams$hasPlayedSound && !itemEntity.isOnGround()){
            lootbeams$hasPlayedSound = false;
        }
    }
}
