package ru.aiefu.fabricrestart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

public class ConfigManager {

    public void gen() throws IOException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.writeValue(new File("./config/fabric-restart.yml"), new Config());
    }

    public Config read() throws IOException {
        return new ObjectMapper(new YAMLFactory()).readValue(new File("./config/fabric-restart.yml"), Config.class);
    }

    public static class Config{
        public final boolean enableScript = false;
        public final String pathToScript = "restart.bat";

        public final List<String> timestamps = new ArrayList<>();
        public final int countdown;
        public final String countdownMsg = "Server restart in %s seconds";
        public final String disconnectMessage = "Server is restarting, we will be back in few minutes";
        public final HashMap<Long, String> messages = new HashMap<>();

        public final boolean enableTpsWatcher = false;
        public final float tpsThreshold = 15.0F;
        public final int tpsDelay = 60;
        public final boolean tpsKillInstantly = false;
        public final String tpsKillMsg = "Server will restart in 20 seconds due to consistently low tps";

        public final boolean enableMemoryWatcher = false;
        public final long memThreshold = 1024;
        public final boolean memKillInstantly = false;
        public final String memKillMsg = "Server will restart in 20 seconds due to high memory usage";
        public final boolean restartNoPlayers = false;
        public final boolean afterLastPlayer = true;
        public final int delay = 300;




        public Config(){
            this.countdown = 15;
            timestamps.add("2:00");
            timestamps.add("8:00");
            timestamps.add("14:00");
            timestamps.add("20:00");
            messages.put(900L, "Server restart in 15 minutes");
            messages.put(300L, "Server restart in 5 minutes");
            messages.put(60L, "Server restart in 1 minute");
        }

        @SuppressWarnings("all")
        public void setup(MinecraftServer server) throws Exception {
            ArrayList<Long> timeList = new ArrayList<>();
            LocalDateTime time = LocalDateTime.now();
            OffsetDateTime offset = OffsetDateTime.now();
            long ms = time.toEpochSecond(offset.getOffset()) * 1000;
            for(String s : this.timestamps){
                int index = s.indexOf(':');
                int hour = Integer.parseInt(s.substring(0, index));
                int minutes = Integer.parseInt(s.substring(index + 1));
                timeList.add(time.withHour(hour).withMinute(minutes).toEpochSecond(offset.getOffset()) * 1000L);
            }

            Collections.sort(timeList);
            boolean disableRestart = false;
            if(!timeList.isEmpty()) {
                timeList.add(timeList.get(0) + 86_400_000);
                Collections.sort(timeList);
            } else disableRestart = true;

            if(!disableRestart) {
                long restart = 0;
                for (long l : timeList) {
                    if (ms < l) {
                        restart = l;
                        break;
                    }
                }
                if (restart <= 0) {
                    throw new Exception("Restart time cannot be lower or equals 0");
                }

                List<Long> msgt = new ArrayList<>();
                for(long l : messages.keySet()){
                    msgt.add(restart - (l * 1000));
                }
                Collections.sort(msgt);
                HashMap<Long, String> msgs = new HashMap<>();
                for (Map.Entry<Long, String> e : messages.entrySet()){
                    msgs.put(restart - (e.getKey() * 1000), e.getValue());
                }
                FabricRestart.rdata = new RestartDataHolder(restart, restart - countdown * 1000L, msgs, msgt, this);
            }
            if(enableMemoryWatcher || enableTpsWatcher){
                new ServerWatcher(server, this, FabricRestart.rdata);
            }
            if(restartNoPlayers){
                FabricRestart.tracker = new PlayerCountTracker(afterLastPlayer, delay, System.currentTimeMillis());
            }
        }
    }
}
