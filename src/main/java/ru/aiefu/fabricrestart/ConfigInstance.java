package ru.aiefu.fabricrestart;

import java.util.ArrayList;

public class ConfigInstance {

    ArrayList<String> timeArray = new ArrayList<>();
    boolean enableRestartScript = false;
    String pathToScript = "./restart.bat";

    public ConfigInstance() {
        timeArray.add("2:00");
        timeArray.add("8:00");
        timeArray.add("14:00");
        timeArray.add("20:00");
    }
}
