package me.justahuman.vaultlootbeams.client;

import me.justahuman.vaultlootbeams.VaultLootBeams;
import me.justahuman.vaultlootbeams.utils.Utils;
import me.justahuman.vaultlootbeams.client.types.BeamRenderMode;
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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.List;
import java.util.Random;

import static me.justahuman.vaultlootbeams.config.ModConfig.CONFIG;

public class LootBeamRenderer extends RenderType {
	private static final ResourceLocation LOOT_BEAM_TEXTURE = new ResourceLocation(VaultLootBeams.MODID, "textures/entity/loot_beam.png");
	private static final ResourceLocation WHITE_TEXTURE = new ResourceLocation(VaultLootBeams.MODID, "textures/entity/white.png");
	public static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(VaultLootBeams.MODID, "textures/entity/glow.png");

	private static final RenderType DEFAULT_BEAM = createBeamRenderType("default", LOOT_BEAM_TEXTURE);
	private static final RenderType SOLID_BEAM = createBeamRenderType("solid", WHITE_TEXTURE);
	private static final RenderType GLOWING_BEAM = createGlowingBeamRenderType();
	private static final RenderType BEAM_SHADOW = createShadowRenderType();

	private static final Random RANDOM = new Random();

	// Required to use RenderStateShard's fields
	private LootBeamRenderer(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
		super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
	}

	public static void renderLootBeam(PoseStack stack, MultiBufferSource buffer, float pTicks, long worldTime, ItemEntity itemEntity) {
		float beamAlpha = (float) CONFIG.beamAlpha;
		float entityTime = itemEntity.getAge();

		// Fade out when close
		double distanceSqr = Minecraft.getInstance().player.distanceToSqr(itemEntity);
		if (distanceSqr < 1.5f) {
			beamAlpha *= (float) (distanceSqr / 1.5f);
		}

		// Don't render beam if its too transparent
		if (beamAlpha <= 0.05f) {
			return;
		}

		boolean solidBeam = CONFIG.beamRenderMode == BeamRenderMode.SOLID;
		float beamRadius = 0.05f * (float) CONFIG.beamRadius;
		float glowRadius = beamRadius * 1.2f;
		float beamHeight = (float) CONFIG.beamHeight;
		float yOffset = (float) CONFIG.beamYOffset;

		Color color = Utils.getItemColor(itemEntity);
		float r = color.getRed() / 255.0F;
		float g = color.getGreen() / 255.0F;
		float b = color.getBlue() / 255.0F;

		// Beam Rendering Code

		stack.pushPose();

		// Render main beam
		stack.pushPose();
		float rotation = (float) Math.floorMod(worldTime, 40L) + pTicks;
		stack.mulPose(Vector3f.YP.rotationDegrees(rotation * 2.25F - 45.0F));
		stack.translate(0, yOffset, 0);
		stack.translate(0, 1, 0);
		stack.mulPose(Vector3f.XP.rotationDegrees(180));
		renderPart(stack, buffer.getBuffer(getBeam()), r, g, b, beamAlpha, beamHeight, 0.0F, beamRadius, beamRadius, 0.0F, -beamRadius, 0.0F, 0.0F, -beamRadius, false);
		stack.mulPose(Vector3f.XP.rotationDegrees(-180));
		renderPart(stack, buffer.getBuffer(getBeam()), r, g, b, beamAlpha, beamHeight, 0.0F, beamRadius, beamRadius, 0.0F, -beamRadius, 0.0F, 0.0F, -beamRadius, !solidBeam);
		stack.popPose();

		// Render glow around main beam
		stack.pushPose();
		stack.translate(0, yOffset, 0);
		stack.translate(0, 1, 0);
		stack.mulPose(Vector3f.XP.rotationDegrees(180));
		renderPart(stack, buffer.getBuffer(getBeam()), r, g, b, beamAlpha * 0.4f, beamHeight, -glowRadius, -glowRadius, glowRadius, -glowRadius, -beamRadius, glowRadius, glowRadius, glowRadius, false);
		stack.mulPose(Vector3f.XP.rotationDegrees(-180));
		renderPart(stack, buffer.getBuffer(getBeam()), r, g, b, beamAlpha * 0.4f, beamHeight, -glowRadius, -glowRadius, glowRadius, -glowRadius, -beamRadius, glowRadius, glowRadius, glowRadius, !solidBeam);
		stack.popPose();

		if (CONFIG.whiteBeamCenter) {
			stack.pushPose();
			stack.translate(0, yOffset, 0);
			stack.translate(0, 1, 0);
			stack.mulPose(Vector3f.XP.rotationDegrees(180));
			renderPart(stack, buffer.getBuffer(getBeam()), r, g, b, beamAlpha, beamHeight, 0.0F, beamRadius * 0.4f, beamRadius * 0.4f, 0.0F, -beamRadius * 0.4f, 0.0F, 0.0F, -beamRadius * 0.4f, false);
			stack.mulPose(Vector3f.XP.rotationDegrees(-180));
			renderPart(stack, buffer.getBuffer(getBeam()), r, g, b, beamAlpha, beamHeight, 0.0F, beamRadius * 0.4f, beamRadius * 0.4f, 0.0F, -beamRadius * 0.4f, 0.0F, 0.0F, -beamRadius * 0.4f, !solidBeam);
			stack.popPose();
		}

		if (CONFIG.beamShadow && itemEntity.isOnGround()) {
			stack.pushPose();
			stack.translate(0, 0.001f, 0);
			float radius = (float) CONFIG.shadowRadius;
			if (CONFIG.animateShadow) {
				float multiplier = (float) (Mth.sin((entityTime + pTicks) / 10.0F + itemEntity.bobOffs + (float) Math.PI) * 0.75 + 0.75F) * 0.6f;
				beamAlpha *= multiplier;
				radius *= multiplier;
			}
			beamAlpha = Math.min(1, beamAlpha * (float) CONFIG.shadowAlphaMultiplier);
			renderShadow(stack, buffer.getBuffer(BEAM_SHADOW), r, g, b, beamAlpha, radius);
			stack.popPose();
		}
		stack.popPose();

		if (CONFIG.beamNameplate && Utils.passes(CONFIG.nameplateCondition, CONFIG.nameplateWhitelist, CONFIG.nameplateBlacklist, itemEntity.getItem())) {
			renderNameplate(stack, buffer, itemEntity, color);
		}
	}

	public static void spawnParticles(ItemEntity item, int entityTime, Color color) {
		float particleCount = Math.abs(20 - CONFIG.particleCount);
		if (entityTime % particleCount == 0 && !Minecraft.getInstance().isPaused()) {
			addParticle(ModClientEvents.GLOW_TEXTURE, color, 1.0f, CONFIG.particleLifetime,
					RANDOM.nextFloat((float) (0.25f * CONFIG.particleSize), (float) (1.1f * CONFIG.particleSize)),
					new Vec3(RANDOM.nextDouble(item.getX() - CONFIG.particleSpread, item.getX() + CONFIG.particleSpread),
							RANDOM.nextDouble(item.getY() - (CONFIG.particleSpread / 3f), item.getY() + (CONFIG.particleSpread / 3f)),
							RANDOM.nextDouble(item.getZ() - CONFIG.particleSpread, item.getZ() + CONFIG.particleSpread)),
					new Vec3(RANDOM.nextDouble(-CONFIG.particleSpeed / 2.0f, CONFIG.particleSpeed / 2.0f),
							RANDOM.nextDouble(CONFIG.particleSpeed),
							RANDOM.nextDouble(-CONFIG.particleSpeed / 2.0f, CONFIG.particleSpeed / 2.0f)));
		}
	}

	private static void addParticle(ResourceLocation spriteLocation, Color color, float alpha, int lifetime, float size, Vec3 pos, Vec3 motion) {
		Minecraft minecraft = Minecraft.getInstance();
		VFXParticle provider = new VFXParticle(minecraft.level,
				minecraft.particleEngine.textureAtlas.getSprite(spriteLocation),
				color, alpha * 1.5f, lifetime, size, pos, motion);
		minecraft.particleEngine.add(provider);
	}

	private static void renderShadow(PoseStack stack, VertexConsumer builder, float red, float green, float blue, float alpha, float radius) {
		PoseStack.Pose matrixEntry = stack.last();
		Matrix4f matrixPose = matrixEntry.pose();
		Matrix3f matrixNormal = matrixEntry.normal();

		// Draw a quad on the xz plane facing up with a radius of 0.5
		builder.vertex(matrixPose, -radius, (float) 0, -radius).color(red, green, blue, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
		builder.vertex(matrixPose, -radius, (float) 0, radius).color(red, green, blue, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
		builder.vertex(matrixPose, radius, (float) 0, radius).color(red, green, blue, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
		builder.vertex(matrixPose, radius, (float) 0, -radius).color(red, green, blue, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
	}

	private static void renderNameplate(PoseStack stack, MultiBufferSource buffer, ItemEntity itemEntity, Color color) {
		// If player is crouching or looking at the item
		Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
		if (player == null || (!player.isCrouching() && !(CONFIG.nameplateOnLook && Utils.isLookingAt(player, itemEntity, CONFIG.nameplateLookSensitivity)))) {
			return;
		}

		ItemStack itemStack = itemEntity.getItem();
		double yOffset = CONFIG.nameplateYOffset;
		float nametagScale = (float) CONFIG.nameplateScale;
		float foregroundAlpha = (float) CONFIG.nameplateTextAlpha;
		float backgroundAlpha = (float) CONFIG.nameplateBackgroundAlpha;
		int foregroundColor = Utils.color(color, foregroundAlpha).getRGB();
		int backgroundColor = Utils.color(color, backgroundAlpha).getRGB();

		// Render nameplate at heights based on player distance
		stack.pushPose();
		stack.translate(0.0D, Math.min(1D, player.distanceToSqr(itemEntity) * 0.025D) + yOffset, 0.0D);
		stack.mulPose(instance.getEntityRenderDispatcher().cameraOrientation());
		stack.scale(-0.02F * nametagScale, -0.02F * nametagScale, 0.02F * nametagScale);

		// Render stack counts
		Font fontRenderer = instance.font;
		String itemName = StringUtil.stripColor(itemStack.getHoverName().getString());
		if (CONFIG.nameplateIncludeCount) {
			int count = itemStack.getCount();
			if (count > 1) {
				itemName = itemName + " x" + count;
			}
		}

		// Move closer to the player so that we don't render in beam, and render the nameplate
		stack.translate(0, 0, -10);
		renderText(fontRenderer, stack, buffer, itemName, foregroundColor, backgroundColor, backgroundAlpha);

		// Render rarity
		stack.translate(0.0D, 10, 0.0D);
		stack.scale(0.75f, 0.75f, 0.75f);
		boolean renderedRarity = false;
		List<Component> tooltip = Utils.tooltipCache(itemEntity, itemStack);

		if (tooltip.size() >= 2) {
			Component tooltipRarity = tooltip.get(1);
			String rarity = tooltipRarity.getString();

			// Render custom rarities
			if (CONFIG.customNameplateRarities.contains(rarity)) {
				Color rarityColor = Utils.getRawColor(tooltipRarity);
				foregroundColor = Utils.color(rarityColor, foregroundAlpha).getRGB();
				backgroundColor = Utils.color(rarityColor, backgroundAlpha).getRGB();
				renderText(fontRenderer, stack, buffer, rarity, foregroundColor, backgroundColor, backgroundAlpha);
				renderedRarity = true;
			}
		}

		if (!renderedRarity && CONFIG.renderVanillaRarities) {
			Color rarityColor = Utils.getRawColor(tooltip.get(0));
			foregroundColor = Utils.color(rarityColor, foregroundAlpha).getRGB();
			backgroundColor = Utils.color(rarityColor, backgroundAlpha).getRGB();
			String rarity = itemEntity.getItem().getRarity().name().toLowerCase();
			renderText(fontRenderer, stack, buffer, StringUtils.capitalize(rarity), foregroundColor, backgroundColor, backgroundAlpha);
		}

		stack.popPose();
	}

	private static void renderText(Font font, PoseStack stack, MultiBufferSource buffer, String text, int foregroundColor, int backgroundColor, float backgroundAlpha) {
		if (!CONFIG.nameplateOutline) {
			font.drawInBatch(text, (float) (-font.width(text) / 2D), 0f, foregroundColor, false, stack.last().pose(), buffer, false, backgroundColor, 15728864);
			return;
		}

		float w = -font.width(text) / 2f;
		int bg = new Color(0, 0, 0, (int) (255 * backgroundAlpha)).getRGB();

		// Draws background (border) text
		font.draw(stack, text, w + 1f, 0, bg);
		font.draw(stack, text, w - 1f, 0, bg);
		font.draw(stack, text, w, 1f, bg);
		font.draw(stack, text, w, -1f, bg);

		// Draws foreground text in front of border
		stack.translate(0.0D, 0.0D, -0.01D);
		font.draw(stack, text, w, 0, foregroundColor);
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
		return switch(CONFIG.beamRenderMode) {
			case GLOWING -> GLOWING_BEAM;
			case SOLID -> SOLID_BEAM;
			default -> DEFAULT_BEAM;
		};
	}

	private static RenderType createBeamRenderType(String type, ResourceLocation texture) {
		CompositeState state = CompositeState.builder()
				.setShaderState(RENDERTYPE_BEACON_BEAM_SHADER)
				.setTextureState(new TextureStateShard(texture, false, false))
				.setTransparencyState(LIGHTNING_TRANSPARENCY)
				.setWriteMaskState(COLOR_DEPTH_WRITE)
				.createCompositeState(false);
		return RenderType.create("loot_beam_" + type, DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, true, state);
	}

	private static RenderType createGlowingBeamRenderType() {
		CompositeState state = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
				.setWriteMaskState(COLOR_DEPTH_WRITE)
				.setTransparencyState(LIGHTNING_TRANSPARENCY)
				.setOutputState(WEATHER_TARGET)
				.setLightmapState(NO_LIGHTMAP)
				.createCompositeState(false);
		return RenderType.create("loot_beam_glowing", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 1536, false, true, state);
	}
	
	private static RenderType createShadowRenderType() {
		CompositeState state = CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
				.setTextureState(new TextureStateShard(GLOW_TEXTURE, false, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setWriteMaskState(COLOR_WRITE)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);
		return RenderType.create("loot_beam_glow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, state);
	}
}