package ru.aiefu.fabricrestart;

import net.minecraft.server.MinecraftServer;

public class PlayerCountTracker {

    private final boolean afterLastPlayer;
    private final long delay;
    private final long serverStartTime;
    private long targetTime;

    public PlayerCountTracker(boolean afterLastPlayer, int delay ,long serverStartTime){
        this.afterLastPlayer = afterLastPlayer;
        this.delay = delay * 1000L;
        this.serverStartTime = serverStartTime;
    }


    public void playersChecker(MinecraftServer server){
        if(server.getTickCount() % 3000 == 0 && server.getPlayerList().getPlayers().size() < 1) {
            long currentTime = System.currentTimeMillis();
            if (afterLastPlayer) {
                if(currentTime > targetTime){
                    server.halt(false);
                }
            } else if(currentTime > serverStartTime + delay){
                server.halt(false);
            }
        }
    }

    public void setTargetTime(long lastPlayerLeftMs) {
        this.targetTime = lastPlayerLeftMs + delay;
    }
}
