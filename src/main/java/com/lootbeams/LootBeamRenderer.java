package com.lootbeams;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class LootBeamRenderer extends RenderType {

	private static final ResourceLocation LOOT_BEAM_TEXTURE = new ResourceLocation(LootBeams.MODID, "textures/entity/loot_beam.png");
	private static final ResourceLocation WHITE_TEXTURE = new ResourceLocation(LootBeams.MODID, "textures/entity/white.png");

	public static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(LootBeams.MODID, "textures/entity/glow.png");
	private static final RenderType DEFAULT_BEAM = createBeamRenderType(LOOT_BEAM_TEXTURE);
	private static final RenderType DEFAULT_GLOW = RenderType.entityCutout(GLOW_TEXTURE);
	private static final RenderType SOLID_BEAM = createBeamRenderType(WHITE_TEXTURE);
	private static final RenderType GLOWING_BEAM = RenderType.lightning();
	private static final RenderType GLOWING_GLOW = createGlowRenderType();

	private static final Random RANDOM = new Random();

	// Required to use RenderStateShard's fields
	private LootBeamRenderer(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
		super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
	}

	public static void renderLootBeam(PoseStack stack, MultiBufferSource buffer, float pTicks, long worldTime, ItemEntity item) {
		float beamAlpha = Configuration.BEAM_ALPHA.get().floatValue();
		float entityTime = item.tickCount;

		// Fade out when close
		double distanceSqr = Minecraft.getInstance().player.distanceToSqr(item);
		if (distanceSqr < 2f) {
			beamAlpha *= (float) distanceSqr;
		}

		// Don't render beam if its too transparent
		if (beamAlpha <= 0.15f) {
			return;
		}

		float beamRadius = 0.05f * Configuration.BEAM_RADIUS.get().floatValue();
		float glowRadius = beamRadius + (beamRadius * 0.2f);
		float beamHeight = Configuration.BEAM_HEIGHT.get().floatValue();
		float yOffset = Configuration.BEAM_Y_OFFSET.get().floatValue();


		Color color = Utils.getItemColor(item);
		float r = color.getRed() / 255f;
		float g = color.getGreen() / 255f;
		float b = color.getBlue() / 255f;

		// I will rewrite the beam rendering code soon! I promise!

		stack.pushPose();

		//Render main beam
		stack.pushPose();
		float rotation = (float) Math.floorMod(worldTime, 40L) + pTicks;
		stack.mulPose(Vector3f.YP.rotationDegrees(rotation * 2.25F - 45.0F));
		stack.translate(0, yOffset, 0);
		stack.translate(0, 1, 0);
		stack.mulPose(Vector3f.XP.rotationDegrees(180));
		renderPart(stack, buffer.getBuffer(getBeam()), r, g, b, beamAlpha, beamHeight, 0.0F, beamRadius, beamRadius, 0.0F, -beamRadius, 0.0F, 0.0F, -beamRadius, false);
		stack.mulPose(Vector3f.XP.rotationDegrees(-180));
		renderPart(stack, buffer.getBuffer(getBeam()), r, g, b, beamAlpha, beamHeight, 0.0F, beamRadius, beamRadius, 0.0F, -beamRadius, 0.0F, 0.0F, -beamRadius, Configuration.SOLID_BEAM.get());
		stack.popPose();

		//Render glow around main beam
		stack.pushPose();
		stack.translate(0, yOffset, 0);
		stack.translate(0, 1, 0);
		stack.mulPose(Vector3f.XP.rotationDegrees(180));
		renderPart(stack, buffer.getBuffer(getBeam()), r, g, b, beamAlpha * 0.4f, beamHeight, -glowRadius, -glowRadius, glowRadius, -glowRadius, -beamRadius, glowRadius, glowRadius, glowRadius, false);
		stack.mulPose(Vector3f.XP.rotationDegrees(-180));
		renderPart(stack, buffer.getBuffer(getBeam()), r, g, b, beamAlpha * 0.4f, beamHeight, -glowRadius, -glowRadius, glowRadius, -glowRadius, -beamRadius, glowRadius, glowRadius, glowRadius, Configuration.SOLID_BEAM.get());
		stack.popPose();

		if (Configuration.WHITE_CENTER.get()) {
			stack.pushPose();
			stack.translate(0, yOffset, 0);
			stack.translate(0, 1, 0);
			stack.mulPose(Vector3f.XP.rotationDegrees(180));
			renderPart(stack, buffer.getBuffer(getBeam()), r, g, b, beamAlpha, beamHeight, 0.0F, beamRadius * 0.4f, beamRadius * 0.4f, 0.0F, -beamRadius * 0.4f, 0.0F, 0.0F, -beamRadius * 0.4f, false);
			stack.mulPose(Vector3f.XP.rotationDegrees(-180));
			renderPart(stack, buffer.getBuffer(getBeam()), r, g, b, beamAlpha, beamHeight, 0.0F, beamRadius * 0.4f, beamRadius * 0.4f, 0.0F, -beamRadius * 0.4f, 0.0F, 0.0F, -beamRadius * 0.4f, Configuration.SOLID_BEAM.get());
			stack.popPose();
		}

		if (Configuration.GLOW_EFFECT.get() && item.isOnGround()) {
			stack.pushPose();
			stack.translate(0, 0.001f, 0);
			float radius = Configuration.GLOW_EFFECT_RADIUS.get().floatValue();
			if (Configuration.ANIMATE_GLOW.get()) {
				beamAlpha *= (float) ((Math.abs(Math.cos((entityTime + pTicks) / 10f)) * 0.5f + 0.5f) * 1.3f);
				radius *= (float) ((Math.abs(Math.cos((entityTime + pTicks) / 10f) * 0.45f)) * 0.75f + 0.75f);
			}
			renderGlow(stack, buffer.getBuffer(getGlow()), r, g, b, beamAlpha * 0.4f, radius);
			stack.popPose();
		}
		stack.popPose();

		if (Configuration.RENDER_NAMETAGS.get()) {
			renderNameTag(stack, buffer, item, color);
		}

		if (Configuration.PARTICLES.get() && !Configuration.PARTICLE_RARE_ONLY.get() || Utils.isRare(item)) {
			renderParticles(pTicks, item, (int) entityTime, r, g, b);
		}
	}

	private static void renderParticles(float pTicks, ItemEntity item, int entityTime, float r, float g, float b) {
		float particleCount = Math.abs(20- Configuration.PARTICLE_COUNT.get().floatValue());
		if (entityTime % particleCount == 0 && pTicks < 0.3f && !Minecraft.getInstance().isPaused()) {
			addParticle(ModClientEvents.GLOW_TEXTURE, r, g, b, 1.0f, Configuration.PARTICLE_LIFETIME.get(),
					RANDOM.nextFloat((float) (0.25f * Configuration.PARTICLE_SIZE.get()), (float) (1.1f * Configuration.PARTICLE_SIZE.get())),
					new Vec3(RANDOM.nextDouble(item.getX() - Configuration.PARTICLE_RADIUS.get(), item.getX() + Configuration.PARTICLE_RADIUS.get()),
							RANDOM.nextDouble(item.getY() - (Configuration.PARTICLE_RADIUS.get()/3f), item.getY() + (Configuration.PARTICLE_RADIUS.get()/3f)),
							RANDOM.nextDouble(item.getZ() - Configuration.PARTICLE_RADIUS.get(), item.getZ() + Configuration.PARTICLE_RADIUS.get())),
					new Vec3(RANDOM.nextDouble(-Configuration.PARTICLE_SPEED.get()/2.0f, Configuration.PARTICLE_SPEED.get()/2.0f),
							RANDOM.nextDouble(Configuration.PARTICLE_SPEED.get()),
							RANDOM.nextDouble(-Configuration.PARTICLE_SPEED.get()/2.0f, Configuration.PARTICLE_SPEED.get()/2.0f)));
		}
	}

	private static void addParticle(ResourceLocation spriteLocation, float red, float green, float blue, float alpha, int lifetime, float size, Vec3 pos, Vec3 motion) {
		// Make the particle brighter
		alpha *= 1.5f;
		Minecraft minecraft = Minecraft.getInstance();
		VFXParticle provider = new VFXParticle(minecraft.level,
				minecraft.particleEngine.textureAtlas.getSprite(spriteLocation),
				red, green, blue, alpha, lifetime, size, pos, motion, 0, false, true);
		minecraft.particleEngine.add(provider);
	}

	private static void renderGlow(PoseStack stack, VertexConsumer builder, float red, float green, float blue, float alpha, float radius) {
		PoseStack.Pose matrixEntry = stack.last();
		Matrix4f matrixPose = matrixEntry.pose();
		Matrix3f matrixNormal = matrixEntry.normal();

		// Draw a quad on the xz plane facing up with a radius of 0.5
		builder.vertex(matrixPose, -radius, (float) 0, -radius).color(red, green, blue, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
		builder.vertex(matrixPose, -radius, (float) 0, radius).color(red, green, blue, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
		builder.vertex(matrixPose, radius, (float) 0, radius).color(red, green, blue, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
		builder.vertex(matrixPose, radius, (float) 0, -radius).color(red, green, blue, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
	}

	private static void renderNameTag(PoseStack stack, MultiBufferSource buffer, ItemEntity ie, Color color) {
		// If player is crouching or looking at the item
		Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
		if (player == null || (!player.isCrouching()
				&& !(Configuration.RENDER_NAMETAGS_ONLOOK.get()
				&& Utils.isLookingAt(player, ie, Configuration.NAMETAG_LOOK_SENSITIVITY.get())))) {
			return;
		}

		ItemStack itemStack = ie.getItem();
		double yOffset = Configuration.NAMETAG_Y_OFFSET.get();
		float nametagScale = Configuration.NAMETAG_SCALE.get().floatValue();
		float foregroundAlpha = Configuration.NAMETAG_TEXT_ALPHA.get().floatValue();
		float backgroundAlpha = Configuration.NAMETAG_BACKGROUND_ALPHA.get().floatValue();
		int foregroundColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * foregroundAlpha)).getRGB();
		int backgroundColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * backgroundAlpha)).getRGB();

		stack.pushPose();

		// Render nametags at heights based on player distance
		stack.translate(0.0D, Math.min(1D, instance.player.distanceToSqr(ie) * 0.025D) + yOffset, 0.0D);
		stack.mulPose(instance.getEntityRenderDispatcher().cameraOrientation());
		stack.scale(-0.02F * nametagScale, -0.02F * nametagScale, 0.02F * nametagScale);

		// Render stack counts on nametag
		Font fontRenderer = instance.font;
		String itemName = StringUtil.stripColor(Utils.nameCache(ie, itemStack).getString());
		if (Configuration.RENDER_STACKCOUNT.get()) {
			int count = itemStack.getCount();
			if (count > 1) {
				itemName = itemName + " x" + count;
			}
		}

		// Move closer to the player so that we don't render in beam, and render the tag
		stack.translate(0, 0, -10);
		renderText(fontRenderer, stack, buffer, itemName, foregroundColor, backgroundColor, backgroundAlpha);

		// Render small tags
		stack.translate(0.0D, 10, 0.0D);
		stack.scale(0.75f, 0.75f, 0.75f);
		boolean textDrawn = false;
		List<Component> tooltip = Utils.tooltipCache(ie, itemStack);

		if (tooltip.size() >= 2) {
			Component tooltipRarity = tooltip.get(1);
			String rarity = tooltipRarity.getString();

			// Render custom rarities
			if (Configuration.CUSTOM_RARITIES.get().contains(rarity)) {
				Color rarityColor = Configuration.WHITE_RARITIES.get() ? Color.WHITE : Utils.getRawColor(tooltipRarity);
				foregroundColor = new Color(rarityColor.getRed(), rarityColor.getGreen(), rarityColor.getBlue(), (int) (255 * foregroundAlpha)).getRGB();
				backgroundColor = new Color(rarityColor.getRed(), rarityColor.getGreen(), rarityColor.getBlue(), (int) (255 * backgroundAlpha)).getRGB();
				renderText(fontRenderer, stack, buffer, rarity, foregroundColor, backgroundColor, backgroundAlpha);
				textDrawn = true;
			}
		}

		if (!textDrawn && Configuration.VANILLA_RARITIES.get()) {
			Color rarityColor = Utils.getRawColor(tooltip.get(0));
			foregroundColor = new Color(rarityColor.getRed(), rarityColor.getGreen(), rarityColor.getBlue(), (int) (255 * foregroundAlpha)).getRGB();
			backgroundColor = new Color(rarityColor.getRed(), rarityColor.getGreen(), rarityColor.getBlue(), (int) (255 * backgroundAlpha)).getRGB();
			String rarity = ie.getItem().getRarity().name().toLowerCase();
			renderText(fontRenderer, stack, buffer, Utils.capitalize(rarity), foregroundColor, backgroundColor, backgroundAlpha);
		}

		stack.popPose();
	}

	private static void renderText(Font fontRenderer, PoseStack stack, MultiBufferSource buffer, String text, int foregroundColor, int backgroundColor, float backgroundAlpha) {
		if (!Configuration.BORDERS.get()) {
			fontRenderer.drawInBatch(text, (float) (-fontRenderer.width(text) / 2D), 0f, foregroundColor, false, stack.last().pose(), buffer, false, backgroundColor, 15728864);
			return;
		}

		float w = -fontRenderer.width(text) / 2f;
		int bg = new Color(0, 0, 0, (int) (255 * backgroundAlpha)).getRGB();

		// Draws background (border) text
		fontRenderer.draw(stack, text, w + 1f, 0, bg);
		fontRenderer.draw(stack, text, w - 1f, 0, bg);
		fontRenderer.draw(stack, text, w, 1f, bg);
		fontRenderer.draw(stack, text, w, -1f, bg);

		// Draws foreground text in front of border
		stack.translate(0.0D, 0.0D, -0.01D);
		fontRenderer.draw(stack, text, w, 0, foregroundColor);
		stack.translate(0.0D, 0.0D, 0.01D);
	}

	private static void renderPart(PoseStack stack, VertexConsumer builder, float red, float green, float blue, float alpha, float height, float radius1, float radius2, float radius3, float radius4, float radius5, float radius6, float radius7, float radius8, boolean gradient) {
		PoseStack.Pose matrixEntry = stack.last();
		Matrix4f matrixPose = matrixEntry.pose();
		Matrix3f matrixNormal = matrixEntry.normal();
		renderQuad(matrixPose, matrixNormal, builder, red, green, blue, alpha, height, radius1, radius2, radius3, radius4, gradient);
		renderQuad(matrixPose, matrixNormal, builder, red, green, blue, alpha, height, radius7, radius8, radius5, radius6, gradient);
		renderQuad(matrixPose, matrixNormal, builder, red, green, blue, alpha, height, radius3, radius4, radius7, radius8, gradient);
		renderQuad(matrixPose, matrixNormal, builder, red, green, blue, alpha, height, radius5, radius6, radius1, radius2, gradient);
	}

	private static void renderQuad(Matrix4f pose, Matrix3f normal, VertexConsumer builder, float red, float green, float blue, float alpha, float y, float x1, float z1, float x2, float z2, boolean gradient) {
		addVertex(pose, normal, builder, red, green, blue, gradient ? 0.0f : alpha, y, x1, z1, 1f, 0f);
		addVertex(pose, normal, builder, red, green, blue, alpha, 0f, x1, z1, 1f, 1f);
		addVertex(pose, normal, builder, red, green, blue, alpha, 0f, x2, z2, 0f, 1f);
		addVertex(pose, normal, builder, red, green, blue, gradient ? 0.0f : alpha, y, x2, z2, 0f, 0f);
	}

	private static void addVertex(Matrix4f pose, Matrix3f normal, VertexConsumer builder, float red, float green, float blue, float alpha, float y, float x, float z, float u, float v) {
		builder.vertex(pose, x, y, z).color(red, green, blue, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
	}

	private static RenderType getBeam() {
		if (Configuration.GLOW_EFFECT.get()) {
			return GLOWING_BEAM;
		}
		return Configuration.SOLID_BEAM.get() ? SOLID_BEAM : DEFAULT_BEAM;
	}

	private static RenderType getGlow() {
		return Configuration.GLOW_EFFECT.get() ? GLOWING_GLOW : DEFAULT_GLOW;
	}

	private static RenderType createBeamRenderType(ResourceLocation texture) {
		RenderType.CompositeState state = RenderType.CompositeState.builder()
				.setShaderState(RenderStateShard.RENDERTYPE_BEACON_BEAM_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
				.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
				.setWriteMaskState(RenderStateShard.COLOR_WRITE)
				.createCompositeState(false);
		return RenderType.create("loot_beam", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, true, state);
	}
	
	private static RenderType createGlowRenderType() {
		RenderType.CompositeState state = RenderType.CompositeState.builder()
				.setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(GLOW_TEXTURE, false, false))
				.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
				.setCullState(RenderStateShard.NO_CULL)
				.setLightmapState(RenderStateShard.LIGHTMAP)
				.setWriteMaskState(RenderStateShard.COLOR_WRITE)
				.setOverlayState(RenderStateShard.OVERLAY)
				.createCompositeState(true);
		return RenderType.create("loot_beam_glow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, state);
	}

}