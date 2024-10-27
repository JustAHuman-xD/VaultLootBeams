package com.lootbeams.client;

import com.lootbeams.LootBeams;
import com.lootbeams.config.ConfigScreen;
import com.lootbeams.utils.Utils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.lootbeams.config.ModConfig.CONFIG;

@Mod.EventBusSubscriber(modid = LootBeams.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientSetup {
	private static final String KEYBIND_PREFIX = "key.lootbeams.";
	private static final String KEYBINDS_CATEGORY = "key.categories.lootbeams";

	public static final KeyMapping OPEN_CONFIG = new KeyMapping(KEYBIND_PREFIX + "open_config", InputConstants.KEY_L, KEYBINDS_CATEGORY);
	public static final KeyMapping RELOAD_CONFIG = new KeyMapping(KEYBIND_PREFIX + "reload_config", InputConstants.KEY_L, KEYBINDS_CATEGORY);

	public static void init(FMLClientSetupEvent ignored) {
		CONFIG.loadFromFile();
		OPEN_CONFIG.setKeyModifierAndCode(KeyModifier.ALT, InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_L));
		RELOAD_CONFIG.setKeyModifierAndCode(KeyModifier.CONTROL, InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_L));
		MinecraftForge.EVENT_BUS.register(ClientSetup.class);
		ClientRegistry.registerKeyBinding(OPEN_CONFIG);
		ClientRegistry.registerKeyBinding(RELOAD_CONFIG);
	}

	@SubscribeEvent
	public static void onItemCreation(EntityJoinWorldEvent event){
		if (event.getEntity() instanceof ItemEntity itemEntity) {
			Utils.cache(itemEntity);
		}
	}

	@SubscribeEvent
	public static void onItemDeletion(EntityLeaveWorldEvent event) {
		if (event.getEntity() instanceof ItemEntity itemEntity) {
			Utils.unCache(itemEntity);
		}
	}

	@SubscribeEvent
	public static void onRenderNameplate(RenderNameplateEvent event) {
		if (event.getEntity() instanceof ItemEntity itemEntity && Utils.rendersBeam(itemEntity)) {
			LootBeamRenderer.renderLootBeam(event.getPoseStack(), event.getMultiBufferSource(), event.getPartialTick(), itemEntity.level.getGameTime(), itemEntity);
		}
	}

	@SubscribeEvent
	public static void onInput(InputEvent.KeyInputEvent event) {
		if (OPEN_CONFIG.consumeClick()) {
			Minecraft.getInstance().setScreen(ConfigScreen.create());
		} else if (RELOAD_CONFIG.consumeClick()) {
			CONFIG.loadFromFile();
		}
	}
}
