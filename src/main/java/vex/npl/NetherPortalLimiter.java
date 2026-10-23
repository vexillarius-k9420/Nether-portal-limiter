package vex.npl;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class NetherPortalLimiter implements ModInitializer {
	public static final String MOD_ID = "nether-portal-limiter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		vex.npl.config.ConfigManager.load();
		LOGGER.info("Hello Fabric world! :P");

		// Регистрируем коллбэк для создания команд
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
				// Главная команда: /npl
				CommandManager.literal("npl")
					.executes(context -> {
						context.getSource().sendFeedback(() -> Text.literal("Nether portal limiter - mod for limitation Nether portals.").formatted(Formatting.DARK_PURPLE), false);
						return 1;
					})
					.then(CommandManager.literal("get")
						.then(CommandManager.literal("stability")
							.executes(context -> {
								Entity entity = context.getSource().getPlayerOrThrow();
								float probability = ProbabilityCalculation.getProbability(entity);
								context.getSource().sendFeedback(
									() -> Text.literal("Stability: "+String.format("%.2f", (1-probability)*100)+"%\n(Probability of the portal functioning)")
										.formatted(Formatting.DARK_PURPLE),
								false
								);
								return 1;
							})
						)
					)
			);
		});
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
