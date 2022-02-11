package ru.aiefu.fabricrestart;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ServerWatcher {
    private final RestartDataHolder rda;
    private final boolean enableTpsW;
    private final boolean enableMemW;

    //Tps Watcher
    private final float tpsThreshold;
    private final int tpsDelay;
    private final boolean killInstantly;
    private final String killMsg;

    private int tpsWatcherTimer;
    private boolean tpsWatcherTriggered;

    //MemoryWatcher
    private final long memThreshold;
    private final boolean killInstantlyM;
    private final String memKillMsg;

    private boolean memWatcherTriggered;

    public ServerWatcher(MinecraftServer server, ConfigManager.Config cfg, RestartDataHolder rda){

        this.rda = rda;
        this.enableTpsW = cfg.enableTpsWatcher;
        this.tpsThreshold = cfg.tpsThreshold;
        this.tpsDelay = cfg.tpsDelay;
        this.killInstantly = cfg.tpsKillInstantly;
        this.killMsg = cfg.tpsKillMsg;

        this.enableMemW = cfg.enableMemoryWatcher;
        this.memThreshold = cfg.memThreshold * 1024 * 1024;
        this.killInstantlyM = cfg.memKillInstantly;
        this.memKillMsg = cfg.memKillMsg;

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("Watcher-thread-%d")
                .setDaemon(true)
                .build();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        if(enableTpsW && enableMemW){
            executor.scheduleAtFixedRate(() -> watch(server), 0L, 1L, TimeUnit.SECONDS);
        } else if(enableTpsW){
            executor.scheduleAtFixedRate(() -> watchTps(server), 0L, 1L, TimeUnit.SECONDS);
        } else if(enableMemW){
            executor.scheduleAtFixedRate(() -> watchMemory(server), 0L, 1L, TimeUnit.SECONDS);
        }
    }
    private void watch(MinecraftServer server){
        watchTps(server);
    }

    private void watchTps(MinecraftServer server){
        double tps = ((ITPS) server).getAverageTPS();
        if(tps < tpsThreshold){
            ++tpsWatcherTimer;
        } else tpsWatcherTimer = 0;
        if(tpsWatcherTimer > tpsDelay && !tpsWatcherTriggered){
            if(!killInstantly) {
                server.getPlayerList().getPlayers().forEach(playerEntity -> playerEntity
                        .sendMessage(new TextComponent(killMsg).withStyle(ChatFormatting.RED), ChatType.SYSTEM,Util.NIL_UUID));
                rda.setRestartTime(System.currentTimeMillis() + 20000);
                rda.disableMessages();
            } else rda.setRestartTime(System.currentTimeMillis());
            tpsWatcherTriggered = true;
        }
    }

    private void watchMemory(MinecraftServer server){
        com.sun.management.OperatingSystemMXBean sys = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        if(sys.getFreeMemorySize() < memThreshold && !memWatcherTriggered){
            if(!killInstantlyM) {
                server.getPlayerList().getPlayers().forEach(playerEntity -> playerEntity
                        .sendMessage(new TextComponent(memKillMsg).withStyle(ChatFormatting.RED), ChatType.SYSTEM, Util.NIL_UUID));
                rda.setRestartTime(System.currentTimeMillis() + 20000);
                rda.disableMessages();
            } else rda.setRestartTime(System.currentTimeMillis());
            memWatcherTriggered = true;
        }
    }
}
