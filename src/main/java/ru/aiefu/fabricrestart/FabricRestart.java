package ru.aiefu.fabricrestart;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import ru.aiefu.fabricrestart.commands.FRCommands;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class FabricRestart implements DedicatedServerModInitializer {

	public static ConfigInstance CONFIG;

	@Override
	public void onInitializeServer() {
		Thread test = new Thread(() -> {
			if(CONFIG.enableRestartScript){
				String osName = System.getProperty("os.name").toLowerCase();
				try {
					if(osName.startsWith("windows")) {
						new ProcessBuilder("cmd.exe", "/c", "call " + CONFIG.pathToScript).start();
					} else new ProcessBuilder(CONFIG.pathToScript).start();
				} catch (Exception e){
					System.out.println("Unable to execute restart script!");
					e.printStackTrace();
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(test);
		ServerLifecycleEvents.SERVER_STARTING.register(this::init);
		ServerLifecycleEvents.SERVER_STARTED.register(this::initRestartThread);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> this.timeRef = System.currentTimeMillis() + CONFIG.getTimeRef());
		ServerLifecycleEvents.SERVER_STOPPING.register(this::initShutdownWatcher);
		ServerTickEvents.END_SERVER_TICK.register(this::playersChecker);
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> FRCommands.register(dispatcher));
	}

	public void init(MinecraftServer server){
		IOManager.genCfg();
		ConfigInstance cfg = IOManager.readCfg();
		try {
			cfg.setup();
		} catch (Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
		CONFIG = cfg;
	}

	public void initRestartThread(MinecraftServer server){
		if(!CONFIG.disableAutoRestart) {
			ThreadFactory threadFactory = new ThreadFactoryBuilder()
					.setNameFormat("Restart-handler-%d")
					.setDaemon(true)
					.build();
			ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
			Runnable r = new Runnable() {
				private int tpsWatcherTimer = 0;
				@Override
				public void run() {
					if(!server.isRunning()){
						return;
					}
					long time = System.currentTimeMillis();
					if(CONFIG.enableMemoryWatcher){
						this.memoryWatcher();
					}
					if (CONFIG.enableTPSWatcher) {
						this.tpsWatcher();
					}
					if (time > CONFIG.RESTART_TIME.get()) {
						server.execute(() -> {
							server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.networkHandler.disconnect(new LiteralText(CONFIG.getDisconnectMessage())));
							server.stop(false);
						});
					} else if (!CONFIG.disableMessages && time > CONFIG.nextMsgTime.get()) {
						String message = CONFIG.messageList.get(CONFIG.msgIndex.get()).getMessage();
						CONFIG.msgIndex.incrementAndGet();
						if(CONFIG.msgIndex.get() < CONFIG.messageList.size())
							CONFIG.nextMsgTime.set(CONFIG.messageList.get(CONFIG.msgIndex.get()).getTime());
						else CONFIG.nextMsgTime.set(CONFIG.RESTART_TIME.get() + 2000);
						server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity.sendSystemMessage(new LiteralText(message).formatted(Formatting.RED), Util.NIL_UUID));
					}
					if (time > CONFIG.COUNTDOWN_TIME.get()) {
						CONFIG.timer.incrementAndGet();
						if (CONFIG.timer.get() >= 2) {
							CONFIG.timer.set(0);
							server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity
									.sendSystemMessage(new LiteralText(String.format(CONFIG.getCountdownMessage(), CONFIG.timer2.get())).formatted(Formatting.RED), Util.NIL_UUID));
							CONFIG.timer2.decrementAndGet();
						}
					}
				}
				private void memoryWatcher(){
					com.sun.management.OperatingSystemMXBean sys = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
					if(sys.getFreePhysicalMemorySize() < CONFIG.memThreshold && !CONFIG.memWatcherTriggered){
						if(!CONFIG.killImmediately) {
							server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity
									.sendSystemMessage(new LiteralText(CONFIG.getMemWatcherKillMessage()).formatted(Formatting.RED), Util.NIL_UUID));
							long restart = System.currentTimeMillis() + 20000;
							CONFIG.RESTART_TIME.set(restart);
							CONFIG.COUNTDOWN_TIME.set(restart - 16000);
						} else CONFIG.RESTART_TIME.set(System.currentTimeMillis());
						CONFIG.memWatcherTriggered = true;
					}
				}
				private void tpsWatcher(){
					double tps = ((ITPS) server).getAverageTPS();
					if(tps < CONFIG.tpsThreshold){
						++tpsWatcherTimer;
					} else tpsWatcherTimer = 0;
					if(tpsWatcherTimer > CONFIG.tpsWatcherDelay && !CONFIG.tpsWatcherTriggered){
						if(!CONFIG.instaKillOnLowTPS) {
							server.getPlayerManager().getPlayerList().forEach(playerEntity -> playerEntity
									.sendSystemMessage(new LiteralText(CONFIG.getTpsWatcherKillMessage()).formatted(Formatting.RED), Util.NIL_UUID));
							long restart = System.currentTimeMillis() + 20000;
							CONFIG.RESTART_TIME.set(restart);
							CONFIG.COUNTDOWN_TIME.set(restart - 16000);
						} else CONFIG.RESTART_TIME.set(System.currentTimeMillis());
						CONFIG.tpsWatcherTriggered = true;
					}
				}
			};
			executor.scheduleAtFixedRate(r, 0, 500, TimeUnit.MILLISECONDS);
		}
	}

	public void initShutdownWatcher(MinecraftServer server){
		if(!CONFIG.enableShutdownWatcher){
			return;
		}
		CONFIG.shutdownWatcherTime += System.currentTimeMillis();
		ThreadFactory threadFactory = new ThreadFactoryBuilder()
				.setNameFormat("Shutdown-watcher-%d")
				.setDaemon(true)
				.build();
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
		executor.scheduleAtFixedRate(() ->{
			if(System.currentTimeMillis() > CONFIG.shutdownWatcherTime){
				System.exit(-1);
			}
		}, 0, 1000, TimeUnit.MILLISECONDS);
	}

	private boolean timeCheckTriggered = false;
	private long timeRef;
	private long timeRef2;

	public void playersChecker(MinecraftServer server){
		if(server.getTicks() % 6000 == 0 && CONFIG.restartWhenNoPlayersAreOnline){
			boolean bl = server.getPlayerManager().getPlayerList().size() < 1;
			long currentTime = System.currentTimeMillis();
			if(CONFIG.inverseMode){
				if(!this.timeCheckTriggered && bl){
					this.timeRef2 = currentTime + CONFIG.getTimeRef();
					this.timeCheckTriggered = true;
				}
				if(!bl) this.timeCheckTriggered = false;

				if(currentTime > this.timeRef2 && bl) server.stop(false);

			} else if (currentTime > this.timeRef && bl) server.stop(false);
		}
	}
}
