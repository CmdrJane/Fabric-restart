package ru.aiefu.fabricrestart;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConfigInstance {
    public List<String> timeArray = new ArrayList<>();

    public boolean enableRestartScript = false;
    public String pathToScript = "restart.bat";
    public transient boolean disableAutoRestart = false;

    public List<Message> messageList = new ArrayList<>();

    public String COUNTDOWN_MESSAGE = "Server restart in %d sec";
    public String MEMORY_WATCHER_MSG = "Not enough physical memory, restarting server in 20 seconds";
    public String TPS_WATCHER_MSG = "Server tps dropped too low, restarting server in 20 seconds";
    public String DISCONNECT_MESSAGE = "Server restarting, will be back in few minutes =)";

    public boolean enableMemoryWatcher = false;
    public boolean killImmediately = false;
    public long memThreshold = 1024;

    public boolean enableTPSWatcher = false;
    public boolean killOnLowTPS = false;
    public double tpsThreshold = 12.0D;

    public boolean enableShutdownWatcher = false;
    public long shutdownWatcherTime = 300L;

    public transient boolean disableMessages = false;
    public transient AtomicInteger msgIndex;
    public transient AtomicLong nextMsgTime;
    public transient AtomicLong COUNTDOWN_TIME;
    public transient AtomicLong RESTART_TIME;

    public transient AtomicInteger timer = new AtomicInteger(0);
    public transient AtomicInteger timer2 = new AtomicInteger(15);

    public transient boolean memWatcherTriggered = false;
    public transient boolean tpsWatcherTriggered = false;

    public ConfigInstance(){
        timeArray.add("2:00");
        timeArray.add("8:00");
        timeArray.add("14:00");
        timeArray.add("20:00");
        messageList.add(new Message(900, "Server restart in 15 minutes"));
        messageList.add(new Message(300, "Server restart in 5 minutes"));
        messageList.add(new Message(60, "Server restart in 1 minute"));
    }
    public String getCountdownMessage(){
        return Objects.requireNonNullElse(COUNTDOWN_MESSAGE, "Server restart in %d sec");
    }
    public String getDisconnectMessage(){
        return Objects.requireNonNullElse(this.DISCONNECT_MESSAGE, "Server restarting, will be back in few minutes =)");
    }
    public String getMemWatcherKillMessage(){
        return Objects.requireNonNullElse(this.MEMORY_WATCHER_MSG, "Not enough physical memory, restarting server in 20 seconds");
    }
    public String getTpsWatcherKillMessage(){
        return Objects.requireNonNullElse(this.TPS_WATCHER_MSG, "Server tps dropped too low, restarting server in 20 seconds");
    }

    public void setup() throws Exception {
        this.memThreshold = this.memThreshold * 1024 * 1024;
        this.shutdownWatcherTime = this.shutdownWatcherTime * 1000;
        ArrayList<Long> timeList = new ArrayList<>();
        for(String s : this.timeArray){
            int index = s.indexOf(':');
            int hour = Integer.parseInt(s.substring(0, index));
            int minutes = Integer.parseInt(s.substring(index + 1));
            timeList.add(LocalDateTime.now().withHour(hour).withMinute(minutes).withSecond(0).toEpochSecond(OffsetDateTime.now().getOffset()) * 1000);
        }
        Collections.sort(timeList);

        if(!timeList.isEmpty()) {
            timeList.add(timeList.get(0) + 86400000);
        } else this.disableAutoRestart = true;

        if(!disableAutoRestart){
            long restart = 0;
            long unixTime = System.currentTimeMillis();
            for(long l : timeList){
                if(unixTime < l){
                    restart = l;
                    break;
                }
            }
            restart = LocalDateTime.now().plusSeconds(80).toEpochSecond(OffsetDateTime.now().getOffset()) * 1000;
            if(restart <= 0){
                throw new Exception("Restart time cannot be lower or equals 0");
            }

            RESTART_TIME = new AtomicLong(restart); //LocalDateTime.now().plusSeconds(70).toEpochSecond(OffsetDateTime.now().getOffset()) * 1000;
            COUNTDOWN_TIME = new AtomicLong(restart - 16000);

            if(!this.messageList.isEmpty()) {
                this.messageList.forEach(Message::convertToEpochSeconds);
            } else this.disableMessages = true;
            setupMessages(restart, unixTime);
        }
    }

    public void setupMessages(long restart, long time){
        if(!disableMessages){
            messageList.forEach(s -> s.setTime(restart - s.getTime()));
            messageList.sort(Comparator.comparing(Message::getTime));
            int i = 0;
            for (Message m : messageList){
                if(time < m.getTime()){
                    msgIndex = new AtomicInteger(i);
                    nextMsgTime = new AtomicLong(m.getTime());
                    break;
                }
                ++i;
            }
        }
    }

    public void delayRestart(long delay){
        RESTART_TIME.set(RESTART_TIME.get() + delay);
        COUNTDOWN_TIME.set(COUNTDOWN_TIME.get() + delay);
        timer.set(0);
        timer2.set(15);
        for(Message m : messageList){
            m.setTime(m.getTime() + delay);
        }
        setupMessages(RESTART_TIME.get(), System.currentTimeMillis());
    }
}
