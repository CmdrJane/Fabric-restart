package ru.aiefu.fabricrestart;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import ru.aiefu.fabricrestart.commands.FRCommands;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FabricRestart implements DedicatedServerModInitializer {
	public static volatile boolean enableRestartScript;
	public static volatile boolean disableAutoRestart = false;

	public static volatile boolean enableMemoryWatcher;
	public static volatile boolean killImmediately;
	public static volatile long memThreshold;

	public static volatile boolean enableTPSWatcher;
	public static volatile boolean killOnLowTPS;
	public static volatile double tpsThreshold;

	public static volatile boolean enableShutdownWatcher;
	public static volatile AtomicLong shutdownWatcherTime;

	public static volatile boolean disableMessages = false;
	public static volatile List<Message> messageList;
	public static volatile AtomicInteger msgIndex;
	public static volatile AtomicLong nextMsgTime;
	public static volatile AtomicLong RESTART_TIME;
	public static volatile String pathToScript;


	public static volatile AtomicLong COUNTDOWN_TIME;
	public static volatile String COUNTDOWN_MESSAGE;
	public static volatile String MEMORY_WATCHER_MSG;
	public static volatile String TPS_WATCHER_MSG;
	public static volatile String DISCONNECT_MESSAGE;
	public static volatile AtomicInteger timer = new AtomicInteger(0);
	public static volatile AtomicInteger timer2 = new AtomicInteger(15);
	@Override
	public void onInitializeServer() {
		Thread test = new Thread(() -> {
			if(enableRestartScript){
				String osName = System.getProperty("os.name").toLowerCase();
				try {
					if(osName.startsWith("windows")) {
						new ProcessBuilder("cmd.exe", "/c", "call " + FabricRestart.pathToScript).start();
					} else new ProcessBuilder(FabricRestart.pathToScript).start();
				} catch (Exception e){
					System.out.println("Unable to execute restart script!");
					e.printStackTrace();
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(test);
		ServerLifecycleEvents.SERVER_STARTING.register(this::init);
		ServerLifecycleEvents.SERVER_STARTED.register(this::initRestartThread);
		ServerLifecycleEvents.SERVER_STOPPING.register(this::initShutdownWatcher);
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> FRCommands.register(dispatcher));
	}

	public void init(MinecraftServer server){
		IOManager.genCfg();
		ArrayList<Long> timeList = IOManager.readCfg();
		if(disableAutoRestart){
			return;
		}
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
		if(!disableMessages){
			long finalRestart = restart;
			messageList.forEach(s -> s.setTime(finalRestart - s.getTime()));
			messageList.sort(Comparator.comparing(Message::getTime));
			int i = 0;
			for (Message m : messageList){
				if(unixTime < m.getTime()){
					msgIndex = new AtomicInteger(i);
					nextMsgTime = new AtomicLong(m.getTime());
					break;
				}
				++i;
			}
		}

		RESTART_TIME = new AtomicLong(restart); //LocalDateTime.now().plusSeconds(70).toEpochSecond(OffsetDateTime.now().getOffset()) * 1000;
		COUNTDOWN_TIME = new AtomicLong(restart - 16000);
	}

	public void initRestartThread(MinecraftServer server){
		if(!disableAutoRestart) {
			ThreadFactory threadFactory = new ThreadFactoryBuilder()
					.setNameFormat("Restart-handler-%d")
					.setDaemon(true)
					.build();
			ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
			Runnable r = new Runnable() {
				@Override
				public void run() {
					if(!server.isRunning()){
						return;
					}
					long time = System.currentTimeMillis();
					if(enableMemoryWatcher){
						this.memoryWatcher();
					}
					if (enableTPSWatcher) {
						this.tpsWatcher();
					}
					if (time > RESTART_TIME.get()) {
						server.execute(() -> {
							server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.networkHandler.disconnect(new LiteralText(DISCONNECT_MESSAGE)));
							server.stop(false);
						});
					} else if (!disableMessages && time > nextMsgTime.get()) {
						String message = messageList.get(msgIndex.get()).getMessage();
						msgIndex.incrementAndGet();
						if(msgIndex.get() < messageList.size())
						nextMsgTime.set(messageList.get(msgIndex.get()).getTime());
						else nextMsgTime.set(RESTART_TIME.get());
						server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.sendSystemMessage(new LiteralText(message).formatted(Formatting.RED), Util.NIL_UUID));
					}
					if (time > COUNTDOWN_TIME.get()) {
						timer.incrementAndGet();
						if (timer.get() >= 2) {
							timer.set(0);
							server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity
									.sendSystemMessage(new LiteralText(String.format(COUNTDOWN_MESSAGE, timer2.get())).formatted(Formatting.RED), Util.NIL_UUID));
							timer2.decrementAndGet();
						}
					}
				}
				private void memoryWatcher(){
					com.sun.management.OperatingSystemMXBean sys = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
					if(sys.getFreeMemorySize() < memThreshold){
						if(!killImmediately) {
							server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity
									.sendSystemMessage(new LiteralText(MEMORY_WATCHER_MSG).formatted(Formatting.RED), Util.NIL_UUID));
							long restart = System.currentTimeMillis() + 20000;
							RESTART_TIME.set(restart);
							COUNTDOWN_TIME.set(restart - 16000);
						} else RESTART_TIME.set(System.currentTimeMillis());
					}
				}
				private void tpsWatcher(){
					double tps = ((ITPS) server).getAverageTPS();
					if(tps < tpsThreshold){
						if(!killOnLowTPS) {
							server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity
									.sendSystemMessage(new LiteralText(TPS_WATCHER_MSG).formatted(Formatting.RED), Util.NIL_UUID));
							long restart = System.currentTimeMillis() + 20000;
							RESTART_TIME.set(restart);
							COUNTDOWN_TIME.set(restart - 16000);
						} else RESTART_TIME.set(System.currentTimeMillis());
					}
				}
			};
			executor.scheduleAtFixedRate(r, 0, 500, TimeUnit.MILLISECONDS);
		}
	}

	public void initShutdownWatcher(MinecraftServer server){
		shutdownWatcherTime.set(System.currentTimeMillis() + shutdownWatcherTime.get());
		ThreadFactory threadFactory = new ThreadFactoryBuilder()
				.setNameFormat("Shutdown-watcher-%d")
				.setDaemon(true)
				.build();
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
		executor.scheduleAtFixedRate(() ->{
			if(System.currentTimeMillis() > shutdownWatcherTime.get()){
				System.exit(-1);
			}
		}, 0, 1000, TimeUnit.MILLISECONDS);
	}

}
