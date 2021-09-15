package ru.aiefu.fabricrestart;

import java.util.ArrayList;
import java.util.List;

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
        if(countdownMessage != null){
            return this.countdownMessage;
        } else return "Server restart in %d sec";
    }
    public String getDisconnectMessage(){
        if(this.restartMessage != null){
            return this.restartMessage;
        } else return "Server restarting, will be back in few minutes =)";
    }
    public String getMemWatcherKillMessage(){
        if(this.memWatcherKillMessage != null){
            return this.memWatcherKillMessage;
        } else return "JVM Consuming too much physical memory, restarting server in 20 seconds";
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
}
