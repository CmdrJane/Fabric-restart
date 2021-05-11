package ru.aiefu.fabricrestart;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.commons.lang3.time.DateUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class FabricRestart implements ModInitializer {
	public static long STARTING_TIME;
	public static long RESTART_TIME;
	public static long FIRST_MESSAGE_TIME;
	public static long SECOND_MESSAGE_TIME;
	public static long COUNTDOWN_TIME;
	public static boolean firstPrint = false;
	public static boolean secondPrint = false;
	public static int timer = 0;
	public static int timer2 = 15;
	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			ZoneOffset timeZone = OffsetDateTime.now().getOffset();
			long mult = 1000;
			STARTING_TIME = LocalDateTime.now().toEpochSecond(timeZone) * mult;
			long millisFirst = LocalDateTime.now().withHour(2).withMinute(0).withSecond(0).toEpochSecond(timeZone) * mult;
			long millisSecond = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).toEpochSecond(timeZone) * mult;
			long millisThird = LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).toEpochSecond(timeZone) * mult;
			long millisFourth = LocalDateTime.now().withHour(20).withMinute(0).withSecond(0).toEpochSecond(timeZone) * mult;
			long millisFifth = LocalDateTime.now().plusDays(1).withHour(2).withMinute(0).withSecond(0).toEpochSecond(timeZone) * mult;

			if(STARTING_TIME < millisFirst){
				RESTART_TIME = millisFirst;
			}
			else if (STARTING_TIME < millisSecond){
				RESTART_TIME = millisSecond;
			}
			else if(STARTING_TIME < millisThird){
				RESTART_TIME = millisThird;
			}
			else if(STARTING_TIME < millisFourth){
				RESTART_TIME = millisFourth;
			}
			else {
				RESTART_TIME = millisFifth;
			}
			FIRST_MESSAGE_TIME = (DateUtils.addMinutes(new Date(RESTART_TIME), -5).getTime());
			SECOND_MESSAGE_TIME = (DateUtils.addMinutes(new Date(RESTART_TIME), -1).getTime());
			COUNTDOWN_TIME = (DateUtils.addSeconds(new Date(RESTART_TIME), -16).getTime());
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			long time = System.currentTimeMillis();
			if(time > RESTART_TIME){
				server.getPlayerManager().getPlayerList().forEach(playerEntity -> {
					playerEntity.networkHandler.disconnect(new LiteralText("Перезапуск сервера, вернемся через несколько минут)"));
				});
				server.stop(false);
			}
			else if(!firstPrint && time > FIRST_MESSAGE_TIME){
				firstPrint = true;
				server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.sendSystemMessage(new LiteralText("Сервер перезапуститься через одну минуту").formatted(Formatting.RED), Util.NIL_UUID));
			}
			else if(!secondPrint && time > SECOND_MESSAGE_TIME){
				secondPrint = true;
				server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.sendSystemMessage(new LiteralText("Сервер перезапуститься через одну минуту").formatted(Formatting.RED), Util.NIL_UUID));
			}
			if(time > COUNTDOWN_TIME){
				++timer;
				if(timer >= 20){
					timer = 0;;
					server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.sendSystemMessage(new LiteralText("Сервер перезапуститься через " +timer2 +" секунд(ы)" ).formatted(Formatting.RED), Util.NIL_UUID));
					--timer2;
				}
			}
		});
	}
}
