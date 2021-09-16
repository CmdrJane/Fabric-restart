package ru.aiefu.fabricrestart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigInstance {

    public List<String> timeArray = new ArrayList<>();
    public List<Message> messageList = new ArrayList<>();
    public boolean enableRestartScript = false;
    public String pathToScript = "restart.bat";
    protected String countdownMessage = "Server restart in %d sec";
    protected String restartMessage = "Server restarting, will be back in few minutes =)";

    protected boolean enableMemoryWatcher = false;
    protected boolean killImmediately = false;
    protected String memWatcherKillMessage = "JVM Consuming too much physical memory, restarting server in 20 seconds";
    protected int memThreshold = 1024;

    protected boolean enableTPSWatcher = false;
    protected boolean tpsKillImmediately = false;
    protected String tpsWatcherKillMessage = "Server tps dropped too low, restarting server in 20 seconds";
    protected double tpsThreshold = 12.0D;

    protected boolean enableShutdownWatcher = false;
    protected int shutdownWatcherDelay = 300;

    public ConfigInstance() {
        timeArray.add("2:00");
        timeArray.add("8:00");
        timeArray.add("14:00");
        timeArray.add("20:00");
        messageList.add(new Message(900, "Server restart in 15 minutes"));
        messageList.add(new Message(300, "Server restart in 5 minutes"));
        messageList.add(new Message(60, "Server restart in 1 minute"));
    }
    public String getCountdownMessage(){
        return Objects.requireNonNullElse(countdownMessage, "Server restart in %d sec");
    }
    public String getDisconnectMessage(){
        return Objects.requireNonNullElse(this.restartMessage, "Server restarting, will be back in few minutes =)");
    }
    public String getMemWatcherKillMessage(){
        return Objects.requireNonNullElse(this.memWatcherKillMessage, "JVM Consuming too much physical memory, restarting server in 20 seconds");
    }
    public long getMemThreshold(){
        return (long) this.memThreshold * 1024 * 1024;
    }
    public boolean getMemWatcher(){
        return this.enableMemoryWatcher;
    }
    public boolean getKillMode(){
        return this.killImmediately;
    }

    public boolean getEnableTPSWatcher(){
        return this.enableTPSWatcher;
    }
    public boolean getTPSKill(){
        return this.tpsKillImmediately;
    }
    public double tpsThreshold(){
        return this.tpsThreshold;
    }
    public String getTpsWatcherKillMessage(){
        return this.tpsWatcherKillMessage;
    }
    public boolean getShutdownWatcher(){
        return this.enableShutdownWatcher;
    }
    public int getShutdownWatcherDelay(){
        return this.shutdownWatcherDelay;
    }
}
