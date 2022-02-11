package ru.aiefu.fabricrestart;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FabricRestart implements DedicatedServerModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("fabricrestart");

	public static RestartDataHolder rdata;
	public static boolean disableShutdownHook = false;

	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			try {
				readConfiguration(server);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(rdata != null){
				if(rdata.isScriptEnabled())
					registerShutdownHook();
			}
		});
	}

	private void readConfiguration(MinecraftServer server) throws Exception {
		ConfigManager m = new ConfigManager();
		if(!Files.exists(Paths.get("./config/fabric-restart.yml"))){
			m.gen();
		}
		m.read().setup(server);
	}

	private void registerShutdownHook(){
		Thread hook = new Thread(() -> {
			if(!disableShutdownHook){
				String osName = System.getProperty("os.name").toLowerCase();
				try {
					if(osName.startsWith("windows")) {
						new ProcessBuilder("cmd.exe", "/c", "call " + rdata.getPathToScript()).start();
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
