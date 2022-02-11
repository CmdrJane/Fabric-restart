package ru.aiefu.fabricrestart;

import net.minecraft.server.MinecraftServer;

public class PlayerCountTracker {

    private final boolean afterLastPlayer;
    private final long delay;
    private final long serverStartTime;
    private final long graceTime;
    private long targetTime;

    public PlayerCountTracker(boolean afterLastPlayer, int delay, int grace, long serverStartTime){
        this.afterLastPlayer = afterLastPlayer;
        this.delay = delay * 1000L;
        this.serverStartTime = serverStartTime;
        this.graceTime = serverStartTime + grace;
    }


    public void playersChecker(MinecraftServer server){
        if(server.getTickCount() % 3000 == 0 && server.getPlayerList().getPlayers().size() < 1) {
            long currentTime = System.currentTimeMillis();
            if(currentTime > graceTime) {
                if (afterLastPlayer) {
                    if (currentTime > targetTime) {
                        server.halt(false);
                    }
                } else if (currentTime > serverStartTime + delay) {
                    server.halt(false);
                }
            }
        }
    }

    public void setTargetTime(long lastPlayerLeftMs) {
        this.targetTime = lastPlayerLeftMs + delay;
    }
}
