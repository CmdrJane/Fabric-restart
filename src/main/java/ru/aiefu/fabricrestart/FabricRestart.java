package ru.aiefu.fabricrestart;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class FabricRestart implements DedicatedServerModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("fabricrestart");

	public static RestartDataHolder rdata;
	public static PlayerCountTracker tracker;
	public static boolean disableShutdownHook = false;
	public static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
			.setNameFormat("FR-Executor-Thread-%d")
			.setDaemon(true)
			.build());

	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			try {
				readConfiguration(server);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
			if(rdata != null){
				if(rdata.isScriptEnabled())
					registerShutdownHook();
			}
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> executor.shutdown());
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> ModCommands.register(dispatcher));
	}

	private void readConfiguration(MinecraftServer server) throws Exception {
		ConfigManager m = new ConfigManager();
		if(!Files.isDirectory(Paths.get("./config"))){
			Files.createDirectory(Paths.get("./config"));
		}
		if(!Files.exists(Paths.get("./config/fabric-restart.yml"))){
			m.gen();
		}
		m.read().setup(server);
	}

	public static void registerShutdownHook(){
		Thread hook = new Thread(() -> {
			if(!disableShutdownHook){
				String osName = System.getProperty("os.name").toLowerCase();
				try {
					if(osName.startsWith("windows")) {
						new ProcessBuilder("cmd.exe", "/c", "start " + rdata.getPathToScript()).start();
					} else new ProcessBuilder(rdata.getPathToScript()).start();
				} catch (IOException e){
					System.out.println("Unable to execute restart script!");
					e.printStackTrace();
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(hook);
	}
}
