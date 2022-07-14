# java-change-log-level-cli
Change log level of Spring loggers runtime via command line - provides OS level authentication, and no JMX is required!

Client example:

```
socat UNIX-CONNECT:/tmp/change-log.sock -
```
Note! See main method for an example. You need to specify user id to the user that is allowed to connect!

