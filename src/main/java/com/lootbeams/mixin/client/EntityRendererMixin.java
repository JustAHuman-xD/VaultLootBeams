package com.lootbeams.mixin.client;

import com.lootbeams.ClientSetup;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {
    @Unique ItemEntity lootbeams$entity;

    @Inject(at = @At("HEAD"), method = "render")
    public void render(T entity, float yaw, float tickDelta, PoseStack stack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        if (entity instanceof ItemEntity itemEntity) {
            lootbeams$entity = itemEntity;
        }
    }

    @ModifyVariable(at = @At("HEAD"), method = "render", ordinal = 0, argsOnly = true)
    public int render(int light) {
        return lootbeams$entity != null ? ClientSetup.overrideLight(lootbeams$entity, light) : light;
    }
}
