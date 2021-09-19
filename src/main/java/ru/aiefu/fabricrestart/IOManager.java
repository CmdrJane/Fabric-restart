package ru.aiefu.fabricrestart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IOManager {
    public static void genCfg(){
        if(!Files.exists(Paths.get("./config/fabric-restart.json"))) {
            String gson = new GsonBuilder().setPrettyPrinting().create().toJson(new ConfigInstance());
            File file = new File("./config/fabric-restart.json");
            write(file, gson);
        }
    }

    public static ConfigInstance readCfg(){
        ConfigInstance configInstance;
        try {
            configInstance = new Gson().fromJson(new FileReader("./config/fabric-restart.json"), ConfigInstance.class);
        } catch (Exception e){
            e.printStackTrace();
            configInstance = new ConfigInstance();
        }
        return configInstance;
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
