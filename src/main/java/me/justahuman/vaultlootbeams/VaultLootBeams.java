package me.justahuman.vaultlootbeams;

import me.justahuman.vaultlootbeams.client.ClientSetup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(VaultLootBeams.MODID)
public class VaultLootBeams {
	public static final String MODID = "vaultlootbeams";
	public static final Logger LOGGER = LogManager.getLogger();
	public static final List<ItemStack> CRASH_BLACKLIST = new ArrayList<>();

	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
	public static final RegistryObject<SoundEvent> LOOT_DROP = register(new SoundEvent(new ResourceLocation(MODID, "loot_drop")));

	public VaultLootBeams() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::setup);
		SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	public static RegistryObject<SoundEvent> register(SoundEvent soundEvent) {
		return SOUNDS.register(soundEvent.getLocation().getPath(), () -> soundEvent);
	}
}
