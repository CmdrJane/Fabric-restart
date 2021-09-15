package ru.aiefu.fabricrestart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IOManager {
    public static void genCfg(){
        if(!Files.exists(Paths.get("./config/fabric-restart.json"))) {
            String gson = new GsonBuilder().setPrettyPrinting().create().toJson(new ConfigInstance());
            File file = new File("./config/fabric-restart.json");
            write(file, gson);
        }
    }

    public static ArrayList<Long> readCfg(){
        ConfigInstance configInstance;
        try {
            configInstance = new Gson().fromJson(new FileReader("./config/fabric-restart.json"), ConfigInstance.class);
        } catch (Exception e){
            e.printStackTrace();
            configInstance = new ConfigInstance();
        }
        ArrayList<Long> timeList = new ArrayList<>();
        for(String s : configInstance.timeArray){
            int index = s.indexOf(':');
            int hour = Integer.parseInt(s.substring(0, index));
            int minutes = Integer.parseInt(s.substring(index + 1));
            timeList.add(LocalDateTime.now().withHour(hour).withMinute(minutes).withSecond(0).toEpochSecond(OffsetDateTime.now().getOffset()) * 1000);
        }
        Collections.sort(timeList);
        if(!timeList.isEmpty()) {
            timeList.add(timeList.get(0) + 86400000);
        } else FabricRestart.disableAutoRestart = true;
        List<Message> msgList = configInstance.messageList;
        if(!msgList.isEmpty()) {
            msgList.forEach(Message::convertToEpochSeconds);
            FabricRestart.messageList = msgList;
        } else FabricRestart.disableMessages = true;

        FabricRestart.enableMemoryWatcher = configInstance.getMemWatcher();
        FabricRestart.killImmediately = configInstance.getKillMode();
        FabricRestart.memThreshold = configInstance.getMemThreshold();
        FabricRestart.enableRestartScript = configInstance.enableRestartScript;
        FabricRestart.pathToScript = configInstance.pathToScript;
        FabricRestart.COUNTDOWN_MESSAGE = configInstance.getCountdownMessage();
        FabricRestart.MEMORY_WATCHER_MSG = configInstance.getMemWatcherKillMessage();
        FabricRestart.DISCONNECT_MESSAGE = configInstance.getDisconnectMessage();
        return timeList;
    }
    public static void write(File file, String gson){
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileWriter writer = new FileWriter(file)) {
            writer.write(gson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
