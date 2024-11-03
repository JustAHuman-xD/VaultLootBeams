package me.justahuman.vaultlootbeams.client;

import me.justahuman.vaultlootbeams.VaultLootBeams;
import me.justahuman.vaultlootbeams.compat.ModCompat;
import me.justahuman.vaultlootbeams.utils.Utils;
import me.justahuman.vaultlootbeams.config.ConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static me.justahuman.vaultlootbeams.config.ModConfig.CONFIG;

@Mod.EventBusSubscriber(modid = VaultLootBeams.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientSetup {
	private static final String KEYBIND_PREFIX = "key." + VaultLootBeams.MODID + ".";
	private static final String KEYBINDS_CATEGORY = "key.categories." + VaultLootBeams.MODID;

	public static final KeyMapping OPEN_CONFIG = new KeyMapping(KEYBIND_PREFIX + "open_config", InputConstants.KEY_L, KEYBINDS_CATEGORY);
	public static final KeyMapping RELOAD_CONFIG = new KeyMapping(KEYBIND_PREFIX + "reload_config", InputConstants.KEY_L, KEYBINDS_CATEGORY);

	public static void init(FMLClientSetupEvent ignored) {
		ModCompat.init();
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
	public static void onInput(InputEvent.KeyInputEvent event) {
		if (OPEN_CONFIG.consumeClick()) {
			Minecraft.getInstance().setScreen(ConfigScreen.create());
		} else if (RELOAD_CONFIG.consumeClick()) {
			CONFIG.loadFromFile();
		}
	}
}
