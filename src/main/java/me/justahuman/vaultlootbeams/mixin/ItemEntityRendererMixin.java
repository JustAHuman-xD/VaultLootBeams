package me.justahuman.vaultlootbeams.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.justahuman.vaultlootbeams.client.LootBeamRenderer;
import me.justahuman.vaultlootbeams.utils.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.justahuman.vaultlootbeams.client.config.ModConfig.CONFIG;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {
    protected ItemEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Unique private boolean vaultLootBeams$rendersBeam;

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public void renderHead(ItemEntity itemEntity, float yaw, float pTicks, PoseStack stack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        this.entityRenderDispatcher.setRenderShadow(true);
        this.vaultLootBeams$rendersBeam = Utils.rendersBeam(itemEntity);
        if (vaultLootBeams$rendersBeam) {
            this.entityRenderDispatcher.setRenderShadow(!CONFIG.beamShadow);
        }
    }

    @Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public void renderTail(ItemEntity itemEntity, float yaw, float pTicks, PoseStack stack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        if (vaultLootBeams$rendersBeam) {
            LootBeamRenderer.renderLootBeam(stack, buffer, pTicks, itemEntity.level.getGameTime(), itemEntity);
        }
    }
}
