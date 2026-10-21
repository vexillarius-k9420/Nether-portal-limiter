package vex.npl;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetherPortalLimiter implements ModInitializer {
	public static final String MOD_ID = "nether-portal-limiter";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		vex.npl.config.ConfigManager.load();
		LOGGER.info("Hello Fabric world! :P");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
