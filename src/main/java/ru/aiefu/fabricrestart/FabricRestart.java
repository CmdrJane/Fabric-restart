package ru.aiefu.fabricrestart;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import ru.aiefu.fabricrestart.commands.FRCommands;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class FabricRestart implements DedicatedServerModInitializer {
	public static boolean enableRestartScript;
	public static boolean disableAutoRestart = false;
	public static String pathToScript;
	public static long RESTART_TIME;
	public static long FIRST_MESSAGE_TIME;
	public static String FIRST_MESSAGE;
	public static long SECOND_MESSAGE_TIME;
	public static String SECOND_MESSAGE;
	public static long COUNTDOWN_TIME;
	public static String COUNTDOWN_MESSAGE;
	public static String DISCONNECT_MESSAGE;
	public static boolean firstPrint = false;
	public static boolean secondPrint = false;
	public static volatile int timer = 0;
	public static volatile int timer2 = 15;
	@Override
	public void onInitializeServer() {
		Thread test = new Thread(() -> {
			if(enableRestartScript){
				String osName = System.getProperty("os.name").toLowerCase();
				try {
					new ProcessBuilder(FabricRestart.pathToScript).start();
					if(osName.startsWith("windows")) {
						new ProcessBuilder("cmd.exe", "/c", "start call " + FabricRestart.pathToScript).start();
					} else new ProcessBuilder(FabricRestart.pathToScript).start();
				} catch (Exception e){
					System.out.println("Unable to execute restart script!");
					e.printStackTrace();
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(test);
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			IOManager.genCfg();
			ArrayList<Long> timeList = IOManager.readCfg();
			long restart = 0;
			long unixTime = System.currentTimeMillis();
			for(long l : timeList){
				if(unixTime < l){
					restart = l;
					break;
				}
			}
			if(restart <= 0){
				server.close();
			}
			RESTART_TIME =  restart; //LocalDateTime.now().plusSeconds(70).toEpochSecond(OffsetDateTime.now().getOffset()) * 1000;
			FIRST_MESSAGE_TIME = restart - 300000;
			SECOND_MESSAGE_TIME = restart - 60000;
			COUNTDOWN_TIME = restart - 16000;
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if(!disableAutoRestart) {
				ThreadFactory threadFactory = new ThreadFactoryBuilder()
						.setNameFormat("Restart-handler-%d")
						.setDaemon(true)
						.build();
				ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
				executor.scheduleAtFixedRate(() -> {
					long time = System.currentTimeMillis();
					if (time > RESTART_TIME) {
						server.execute(() -> {
							server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.networkHandler.disconnect(new LiteralText(DISCONNECT_MESSAGE)));
							server.stop(false);
						});
					} else if (!firstPrint && time > FIRST_MESSAGE_TIME) {
						firstPrint = true;
						server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.sendSystemMessage(new LiteralText(FIRST_MESSAGE).formatted(Formatting.RED), Util.NIL_UUID));
					} else if (!secondPrint && time > SECOND_MESSAGE_TIME) {
						secondPrint = true;
						server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.sendSystemMessage(new LiteralText(SECOND_MESSAGE).formatted(Formatting.RED), Util.NIL_UUID));
					}
					if (time > COUNTDOWN_TIME) {
						++timer;
						if (timer >= 2) {
							timer = 0;
							server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity
									.sendSystemMessage(new LiteralText(String.format(COUNTDOWN_MESSAGE, timer2)).formatted(Formatting.RED), Util.NIL_UUID));
							--timer2;
						}
					}
				}, 0, 500, TimeUnit.MILLISECONDS);
			}
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> FRCommands.register(dispatcher));
	}
}
