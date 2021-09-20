# Fabric Restart

### Restart your server by providing time when it needs to restart!
### Works only on dedicated server

## Setup
### Just drop to your mods folder

## Configuration

 ### "timeArray":[ "2:00", "8:00", "14:00", "20:00" ] - table of timestamps when server should restart. You can add/remove elements if need. Exmaples:
 
### "timeArray":[ "8:00", "16:00", "0:00" ] or "timeArray":[ "12:00", "0:00" ]

 
### "enableRestartScript": - whenever execution of restart script should be enabled. Can be true/false, false by default
### "pathToScript": "restart.bat" - path to your server restart script from root server directory. It was recommended to put restart script in root directory of your server. For linux: "pathToScript": "./restart.sh"


### "messageList": - list of messages that will be send to chat when this much time left until restart. Example

### { "time": 60, "message": "Server restart in 1 minute" }


### "COUNTDOWN_MESSAGE": - Message that will be displayed on 15 second countdown before restart

### "MEMORY_WATCHER_MSG": - Message that will be displayed if memory watcher enabled and triggered if "killImmediately" is not set to true

### "TPS_WATCHER_MSG": - Message that will be displayed if tps falls too low and tps wacther is enabled and triggered if "instaKillOnLowTPS" is not set to true

### "DISCONNECT_MESSAGE": - Message that will be displayed to players when they got kicked right before restart


### "enableMemoryWatcher": - if memory watcher should be enabled. Default false

### "killImmediately": - if watcher should instantly stop(restart if restart script is enabled) the server, default false

### "memThreshold": - if total physical free memory will fall bellow this value watcher will be triggered. Default 1024 (in megabytes)


### "enableTPSWatcher": - if tps watcher should be enabled. Default false
### "instaKillOnLowTPS": - if watcher should instantly stop(restart if restart script is enabled) the server, default false
### "tpsWatcherDelay": - if tps falls bellow threshold watcher will wait this much time in seconds before restarting server. If tps return to normal watcher will reset
### "tpsThreshold": 12.0 -  if tps will fall bellow this value watcher will be triggered


### "enableShutdownWatcher": if shutdown watcher should be enabled. Default false
### "shutdownWatcherTime": 300 - if server shutdown procedure will takes this much time in seconds watcher will instantly kill JVM process


### "restartWhenNoPlayersAreOnline": self explanatory
### "inverseMode": - if false then server will restart if it was running for time specified in restartDelay field and there is no players online. if true then server will restart after time specified in restartDelay field will pass since last player leave from server. Default false
### "restartDelay": 60 - delay in minutes


## License

### Check License file
