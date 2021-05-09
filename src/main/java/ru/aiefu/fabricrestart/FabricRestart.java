package ru.aiefu.fabricrestart;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.time.LocalDateTime;

public class FabricRestart implements ModInitializer {
	public static LocalDateTime STARTING_TIME;
	public static LocalDateTime RESTART_TIME;
	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			STARTING_TIME = LocalDateTime.now();

			if(STARTING_TIME.isBefore(LocalDateTime.now().withHour(2).withMinute(0).withSecond(0))){
				RESTART_TIME = LocalDateTime.now().withHour(2).withMinute(0).withSecond(0);
			}
			else if (STARTING_TIME.isBefore(LocalDateTime.now().withHour(8).withMinute(0).withSecond(0))){
				RESTART_TIME = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0);
			}
			else if(STARTING_TIME.isBefore(LocalDateTime.now().withHour(14).withMinute(0).withSecond(0))){
				RESTART_TIME = LocalDateTime.now().withHour(14).withMinute(0).withSecond(0);
			}
			else if(STARTING_TIME.isBefore(LocalDateTime.now().withHour(20).withMinute(0).withSecond(0))){
				RESTART_TIME = LocalDateTime.now().withHour(20).withMinute(0).withSecond(0);
			}
			else {
				RESTART_TIME = LocalDateTime.now().plusDays(1).withHour(2).withMinute(0).withSecond(0);
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if(LocalDateTime.now().isAfter(RESTART_TIME)){
				server.stop(false);
			}
		});
	}
}
