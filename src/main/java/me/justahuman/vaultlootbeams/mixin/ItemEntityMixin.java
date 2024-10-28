package me.justahuman.vaultlootbeams.mixin;

import me.justahuman.vaultlootbeams.VaultLootBeams;
import me.justahuman.vaultlootbeams.client.LootBeamRenderer;
import me.justahuman.vaultlootbeams.utils.Utils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.justahuman.vaultlootbeams.config.ModConfig.CONFIG;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow private int age;

    @Unique private boolean vaultLootBeams$playedSound = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        ItemStack itemStack = itemEntity.getItem();
        if (CONFIG.beamParticles && Utils.passes(CONFIG.particleCondition, CONFIG.particleWhitelist, CONFIG.particleBlacklist, itemStack)) {
            LootBeamRenderer.spawnParticles(itemEntity, this.age, Utils.getItemColor(itemEntity));
        }

        if (CONFIG.landingSound && !vaultLootBeams$playedSound && (itemEntity.isOnGround() || (itemEntity.isOnGround() && (itemEntity.tickCount < 10 && itemEntity.tickCount > 3)))) {
            if (Utils.passes(CONFIG.soundCondition, CONFIG.soundWhitelist, CONFIG.soundBlacklist, itemStack)) {
                itemEntity.level.playSound(null, itemEntity, VaultLootBeams.LOOT_DROP.get(), SoundSource.AMBIENT, (float) CONFIG.soundVolume, ((itemEntity.level.random.nextFloat() - itemEntity.level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }
            vaultLootBeams$playedSound = true;
        }
    }
}
