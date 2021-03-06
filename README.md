## isthere

File notifications made simple

### Overview

isthere checks if a file exists otherwise it will send a notification email to the user in `src/main/resources/main.config`. If the file is not present the application will watch
the expected directory until the file arrives. Once it does a second email will be sent out notifying you.

### Setup

First you must declare an environment variables called `ISTHERE_HOME` that points to the isthere home directory. This is OS dependent for instance..

- On windows it might be `%APP_DATA%\isthere\conf`
- On Linux-esque machines it might be `/etc/isthere/conf`

All the configuration values are located in `src/main/resources/main.config` and the following properties must be set in order for the application to function...

- `host` The SMTP host to connect (string)
- `user` The SMTP account username (string)
- `pass` The SMTP account password (string)
- `port` Port number to connect (integer)
- `emailTo` The email address for notification emails to be sent
- `emailFrom` The emaill address to set in the FROM field

### Usage

isthere was built to be run as a `CRON` job however any scheduling application will work with it. To execute the application enter the following in your terminal...

```
# Execute the appropriate wrapper script
$ ./isthere /path/to/file.txt

# Windows..
$ ./isthere.bat /path/to/file.txt
```

NOTE: If the file is present the application will immediately exit otherwise the application will run until the file arrives.
