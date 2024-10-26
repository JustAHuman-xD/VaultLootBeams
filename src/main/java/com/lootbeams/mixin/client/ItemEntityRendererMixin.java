package com.lootbeams.mixin.client;

import com.lootbeams.utils.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.lootbeams.config.ModConfig.CONFIG;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {
    @Unique private ItemEntity lootbeams$entity;

    protected ItemEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public void render(ItemEntity itemEntity, float yaw, float tickDelta, PoseStack stack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        this.entityRenderDispatcher.setRenderShadow(true);
        if (Utils.rendersBeam(itemEntity)) {
            this.lootbeams$entity = itemEntity;
            this.entityRenderDispatcher.setRenderShadow(!CONFIG.beamShadow);
        }
    }

    @ModifyVariable(at = @At("HEAD"), method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", ordinal = 0, argsOnly = true)
    public int render(int light) {
        return this.lootbeams$entity != null ? 15728640 : light;
    }
}
