package me.justahuman.vaultlootbeams.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class VFXParticle extends TextureSheetParticle {
    private static final int LIGHT_COLOR = LightTexture.pack(15, 15);

    public VFXParticle(ClientLevel clientWorld, TextureAtlasSprite sprite, Color color, float a, int lifetime, float size, Vec3 pos, Vec3 motion) {
        super(clientWorld, pos.x, pos.y, pos.z);
        this.setSprite(sprite);
        this.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
        this.alpha = a;
        this.lifetime = lifetime;
        this.quadSize = size / 10;
        this.setSize(size / 10, size / 10);
        this.xd = motion.x;
        this.yd = motion.y;
        this.zd = motion.z;
        this.gravity = 0;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.alpha -= (float) 1 / this.lifetime;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.yd -= 0.02D * (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);
        }
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float pPartialTick) {
        return LIGHT_COLOR;
    }
}