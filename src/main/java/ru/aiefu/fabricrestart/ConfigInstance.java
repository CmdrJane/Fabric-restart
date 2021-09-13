package ru.aiefu.fabricrestart;

import java.util.ArrayList;

public class ConfigInstance {

    ArrayList<String> timeArray = new ArrayList<>();
    boolean enableRestartScript = false;
    String pathToScript = "restart.bat";
    String fiveMinMessage = "Server restart in 5 minutes";
    String oneMinMessage = "Server restart in 1 minute";
    String countdownMessage = "Server restart in %d sec";
    String restartMessage = "Server restarting, will be back in few minutes =)";

    public ConfigInstance() {
        timeArray.add("2:00");
        timeArray.add("8:00");
        timeArray.add("14:00");
        timeArray.add("20:00");
    }
    public String getFiveMinMessage(){
        if(fiveMinMessage != null){
            return this.fiveMinMessage;
        } else return "Server restart in 5 minutes";
    }
    public String getOneMinMessage(){
        if(oneMinMessage != null){
            return this.oneMinMessage;
        } else return "Server restart in 1 minute";
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
}
