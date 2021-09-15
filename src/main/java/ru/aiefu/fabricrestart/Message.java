package ru.aiefu.fabricrestart;

public class Message {
    protected long time;
    protected String message;
    public Message(long time, String msg){
        this.time = time;
        this.message = msg;
    }
    public long getTime(){
        return this.time;
    }
    public String getMessage(){
        return this.message;
    }
    public void convertToEpochSeconds(){
        this.time = time * 1000;
    }

    public void addTime(long time){
        this.time += time;
    }
    public void setTime(long time){
        this.time = time;
    }
}
