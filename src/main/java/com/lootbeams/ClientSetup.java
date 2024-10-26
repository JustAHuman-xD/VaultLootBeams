package com.lootbeams;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.config.ui.ConfigHelper;
import com.simibubi.create.foundation.config.ui.SubMenuConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = LootBeams.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientSetup {
	private static final String KEYBIND_PREFIX = "key.lootbeams.";
	private static final String KEYBINDS_CATEGORY = "key.categories.lootbeams";
	private static final List<Consumer<PoseStack>> RENDERERS = new ArrayList<>();

	public static final KeyMapping OPEN_CONFIG = new KeyMapping(KEYBIND_PREFIX + "open_config", InputConstants.KEY_L, KEYBINDS_CATEGORY);

	public static void init(FMLClientSetupEvent ignored) {
		OPEN_CONFIG.setKeyModifierAndCode(KeyModifier.ALT, InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_L));
		MinecraftForge.EVENT_BUS.register(ClientSetup.class);
		ClientRegistry.registerKeyBinding(OPEN_CONFIG);
	}

	@SubscribeEvent
	public static void onItemCreation(EntityJoinWorldEvent event){
		if (event.getEntity() instanceof ItemEntity ie) {
			Utils.cache(ie);
		}
	}

	@SubscribeEvent
	public static void onItemDeletion(EntityLeaveWorldEvent event) {
		if (event.getEntity() instanceof ItemEntity ie) {
			Utils.unCache(ie);
		}
	}

	@SubscribeEvent
	public static void onRenderNameplate(RenderNameplateEvent event) {
		if (!(event.getEntity() instanceof ItemEntity itemEntity)
				|| Minecraft.getInstance().player.distanceToSqr(itemEntity) > Math.pow(Configuration.RENDER_DISTANCE.get(), 2)) {
			return;
		}

		Item item = itemEntity.getItem().getItem();
		boolean shouldRender = (Configuration.ALL_ITEMS.get()
				|| (Configuration.ONLY_EQUIPMENT.get() && Utils.isEquipmentItem(item))
				|| (Configuration.ONLY_RARE.get())
				|| (Utils.isItemInRegistryList(Configuration.WHITELIST.get(), itemEntity.getItem().getItem())))
				&& !Utils.isItemInRegistryList(Configuration.BLACKLIST.get(), itemEntity.getItem().getItem());

		if (shouldRender && (!Configuration.REQUIRE_ON_GROUND.get() || itemEntity.isOnGround())) {
			RENDERERS.add(stack -> {
				stack.pushPose();
				stack.translate(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ());
				LootBeamRenderer.renderLootBeam(stack, event.getMultiBufferSource(), event.getPartialTick(), itemEntity.level.getGameTime(), itemEntity);
				stack.popPose();
			});
		}
	}

	@SubscribeEvent
	public static void onLevelRender(RenderLevelStageEvent event) {
        RenderLevelStageEvent.Stage stage = event.getStage();
		if (stage == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
			PoseStack stack = event.getPoseStack();
			Vec3 camera = event.getCamera().getPosition();
			stack.pushPose();
			stack.translate(-camera.x, -camera.y, -camera.z);
			for (Consumer<PoseStack> renderer : RENDERERS) {
				renderer.accept(stack);
			}
			stack.popPose();
		} else if (stage == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
			RENDERERS.clear();
		}
	}

	@SubscribeEvent
	public static void onKeybind(InputEvent.KeyInputEvent event) {
		if (OPEN_CONFIG.consumeClick()) {
			SubMenuConfigScreen configScreen = SubMenuConfigScreen.find(ConfigHelper.ConfigPath.parse(LootBeams.MODID + ":client"));
			Minecraft.getInstance().setScreen(configScreen);
		}
	}

	public static int overrideLight(ItemEntity ie, int light) {
		if (Configuration.ALL_ITEMS.get()
				|| (Configuration.ONLY_EQUIPMENT.get() && Utils.isEquipmentItem(ie.getItem().getItem()))
				|| (Configuration.ONLY_RARE.get() && Utils.isRare(ie))
				|| (Utils.isItemInRegistryList(Configuration.WHITELIST.get(), ie.getItem().getItem()))) {
			light = 15728640;
		}

		return light;
	}

	public static void playDropSound(ItemEntity itemEntity) {
		if (!Configuration.SOUND.get()) {
			return;
		}

		Item item = itemEntity.getItem().getItem();
		if ((Configuration.SOUND_ALL_ITEMS.get() && !Utils.isItemInRegistryList(Configuration.BLACKLIST.get(), item))
				|| (Configuration.SOUND_ONLY_EQUIPMENT.get() && Utils.isEquipmentItem(item))
				|| (Configuration.SOUND_ONLY_RARE.get() && Utils.isRare(itemEntity))
				|| Utils.isItemInRegistryList(Configuration.SOUND_ONLY_WHITELIST.get(), item)) {
			itemEntity.level.playSound(null, itemEntity, LootBeams.LOOT_DROP.get(), SoundSource.AMBIENT, Configuration.SOUND_VOLUME.get().floatValue(), ((itemEntity.level.random.nextFloat() - itemEntity.level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
		}
	}

}
