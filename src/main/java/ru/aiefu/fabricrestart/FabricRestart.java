package ru.aiefu.fabricrestart;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import ru.aiefu.fabricrestart.commands.RestartCommand;

import java.util.ArrayList;

public class FabricRestart implements DedicatedServerModInitializer {
	public static boolean enableRestartScript;
	public static boolean shouldRestart = false;
	public static String pathToScript;
	public static long RESTART_TIME;
	public static long FIRST_MESSAGE_TIME;
	public static long SECOND_MESSAGE_TIME;
	public static long COUNTDOWN_TIME;
	private static boolean firstPrint = false;
	private static boolean secondPrint = false;
	private static boolean stopExecuted = false;
	private static int timer = 0;
	private static int timer2 = 15;
	@Override
	public void onInitializeServer() {
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
			RESTART_TIME = restart;
			FIRST_MESSAGE_TIME = restart - 300000;
			SECOND_MESSAGE_TIME = restart - 60000;
			COUNTDOWN_TIME = restart - 16000;
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			long time = System.currentTimeMillis();
			if(time > RESTART_TIME && !stopExecuted){
				stopExecuted = true;
				server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.networkHandler.disconnect(new LiteralText("Перезапуск сервера, вернемся через несколько минут)")));
				if(enableRestartScript){
					shouldRestart = true;
				}
				server.stop(false);
			}
			else if(!firstPrint && time > FIRST_MESSAGE_TIME){
				firstPrint = true;
				server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.sendSystemMessage(new LiteralText("Сервер перезапуститься через пять минут").formatted(Formatting.RED), Util.NIL_UUID));
			}
			else if(!secondPrint && time > SECOND_MESSAGE_TIME){
				secondPrint = true;
				server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.sendSystemMessage(new LiteralText("Сервер перезапуститься через одну минуту").formatted(Formatting.RED), Util.NIL_UUID));
			}
			if(time > COUNTDOWN_TIME){
				++timer;
				if(timer >= 20){
					timer = 0;
					server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.sendSystemMessage(new LiteralText("Сервер перезапуститься через " +timer2 +" секунд(ы)").formatted(Formatting.RED), Util.NIL_UUID));
					--timer2;
				}
			}
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			RestartCommand.register(dispatcher);
		});
	}
}
