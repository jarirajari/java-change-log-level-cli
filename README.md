# java-change-log-level-cli
Change log level of Spring loggers runtime via command line - provides OS level authentication, and no JMX is required!

# Example
First, open terminal and check your username (that is the domain socket user):

```
whoami
```

Server example (testing, without real integration):

You can start server in IDE and give command line argument for the username that was learned with 'whoami' command.

Client example:

```
bash socket-client.sh
```
Note! See main method for an example. You need to specify user id to the user that is allowed to connect!
      'Errno98' means that a Port is already in use by another application !!! 

In case you have successfully integrated and connected, you will see the following in the terminal.

```
bash socket-client.sh 
Usage: level [<logger name> <new level>] To Quit type 'quit'
Levels: ALL, FINEST, FINER, FINE, CONFIG, INFO, SEVERE, OFF
$> 
```

Then you can explore found loggers using command 'level'. It prints the found loggers:

```
$> level
level
Logger 'org.jooq.tools.LoggerListener' with level 'SEVERE' 
Logger 'org.apache' with level 'WARNING' 
Logger 'org.springframework.orm.jpa' with level 'SEVERE' 
Logger 'org.eclipse.jetty.util.component.AbstractLifeCycle' with level 'SEVERE' 
Logger 'org.hibernate' with level 'SEVERE' 
Logger 'org.apache.sshd.common.util.SecurityUtils' with level 'WARNING' 
Logger 'org.springframework.transaction' with level 'SEVERE' 
Logger 'org.springframework.jdbc' with level 'SEVERE' 
Logger 'org.ehcache' with level 'SEVERE' 
Logger 'AUDIT_LOG' with level 'INFO' 
Logger 'org.springframework.orm.jpa.JpaTransactionManager' with level 'SEVERE' 
Logger 'org.apache.tomcat.util.net.NioSelectorPool' with level 'WARNING' 
Logger 'org.jooq' with level 'SEVERE' 
Logger 'org.apache.catalina.util.LifecycleBase' with level 'SEVERE' 
Logger 'org.hibernate.validator.internal.util.Version' with level 'WARNING' 
Logger 'EMAIL_LOG' with level 'INFO' 
Logger 'io.swagger.jaxrs' with level 'SEVERE' 
Logger 'org.springframework.boot.actuate.endpoint.jmx' with level 'WARNING' 
Logger 'net.sf.ehcache' with level 'SEVERE' 
Logger 'org.hibernate.SQL' with level 'SEVERE' 
Logger 'org.apache.coyote.http11.Http11NioProtocol' with level 'WARNING' 
Logger 'org.apache.catalina.startup.DigesterFactory' with level 'SEVERE' 
Logger '' with level 'INFO' 
```

Next, I am going to change log level of 'org.springframework.jdbc' from 'SEVERE' to 'INFO'

```
$> level org.springframework.jdbc INFO
level org.springframework.jdbc INFO
```

To verify the change list all loggers again:

```
$> level
level
Logger 'org.springframework.jdbc' with level 'INFO'
```

To quit and exit:

```
$> quit
quit
```

# Integration
1. Add dependency to the host application

```
<dependency>
   <groupId>com.horcruxid</groupId>
   <artifactId>java-change-log-level-cli</artifactId>
   <version>1.0.0-SNAPSHOT</version>
   <scope>compile</scope>
</dependency>
```

2. Initialize it in the host application

```
var domainuser = "sysadmin";
com.horcruxid.main.Application.integrationStart(domainuser);
```

3. Finalize it in the host application (if needed)

```
com.horcruxid.main.Application.integrationStop();
```

