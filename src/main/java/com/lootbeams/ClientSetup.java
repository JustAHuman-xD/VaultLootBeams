package com.lootbeams;

import com.lootbeams.config.Configuration;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.foundation.config.ui.ConfigHelper;
import com.simibubi.create.foundation.config.ui.SubMenuConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

@Mod.EventBusSubscriber(modid = LootBeams.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientSetup {
	private static final String KEYBIND_PREFIX = "key.lootbeams.";
	private static final String KEYBINDS_CATEGORY = "key.categories.lootbeams";

	public static final KeyMapping OPEN_CONFIG = new KeyMapping(KEYBIND_PREFIX + "open_config", InputConstants.KEY_L, KEYBINDS_CATEGORY);

	public static void init(FMLClientSetupEvent ignored) {
		OPEN_CONFIG.setKeyModifierAndCode(KeyModifier.ALT, InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_L));
		MinecraftForge.EVENT_BUS.register(ClientSetup.class);
		ClientRegistry.registerKeyBinding(OPEN_CONFIG);
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
	public static void onKeybind(InputEvent.KeyInputEvent event) {
		if (OPEN_CONFIG.consumeClick()) {
			SubMenuConfigScreen configScreen = SubMenuConfigScreen.find(ConfigHelper.ConfigPath.parse(LootBeams.MODID + ":client"));
			Minecraft.getInstance().setScreen(configScreen);
		}
	}

	public static void playDropSound(ItemEntity itemEntity) {
		if (!Configuration.SOUND.get()) {
			return;
		}

		ItemStack itemStack = itemEntity.getItem();
		Item item = itemStack.getItem();
		if ((Configuration.SOUND_ALL_ITEMS.get() && !Utils.isItemInRegistryList(Configuration.SOUND_ONLY_BLACKLIST.get(), item))
				|| (Configuration.SOUND_ONLY_EQUIPMENT.get() && Utils.isEquipmentItem(item))
				|| (Configuration.SOUND_ONLY_RARE.get() && Utils.isRare(itemStack))
				|| Utils.isItemInRegistryList(Configuration.SOUND_ONLY_WHITELIST.get(), item)) {
			itemEntity.level.playSound(null, itemEntity, LootBeams.LOOT_DROP.get(), SoundSource.AMBIENT, Configuration.SOUND_VOLUME.get().floatValue(), ((itemEntity.level.random.nextFloat() - itemEntity.level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
		}
	}

}
