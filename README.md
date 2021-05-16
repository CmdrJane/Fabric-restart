# Fabric Restart

### Restart your server by providing time when it needs to restart!

## Setup

### timeArray represents the time of day when server will restart. It using local time of machine it running on

### Default:

#### "timeArray":[

#### "2:00",

#### "8:00",

####" 14:00",

#### "20:00" ]

### enableRestartScript specifies whenever restart script should be enabled. if you're running your server as linux service and have configured your service to always restart you want this to be false 


#### "enableRestartScript": false
### pathToScript is a relative path to your restart script. By default your restart.bat script file should be located at same level as the server.properties file. 

### .sh scripts should work too

#### "pathToScript": "./restart.bat",

### Message to be displayed when 5 minutes until restart left

#### "fiveMinMessage": "Server restart in 5 minutes",

### Message to be displayed when 1 minute until restart left

#### "oneMinMessage": "Server restart in 1 minute",

### Restart message displayed in chat on 15 seconds countdown before restart

#### "countdownMessage": "Server restart in %d sec",

###T his message will be displayed to players when they got kicked before restart

#### "restartMessage": "Server restarting, will be back in few minutes \u003d)"



## License

### Check License file
