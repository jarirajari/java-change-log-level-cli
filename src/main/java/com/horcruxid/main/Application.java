package com.horcruxid.main;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.LoggingMXBean;

// https://docs.oracle.com/javase/tutorial/jmx/mbeans/mxbeans.html

// https://docs.oracle.com/en/java/javase/12/management/using-platform-mbean-server-and-platform-mxbeans.html#GUID-E7CCD12F-00C4-4547-B3F4-E9D969768C9E
public class Application {

    private static Logger log = Logger.getLogger(Application.class.getName());

    private static MBeanServer mbs;
    private static LoggingMXBean lbs;

    public Application() {
        mbs = ManagementFactory.getPlatformMBeanServer();
        lbs = LogManager.getLoggingMXBean();
    }

    /**
     * TODO
     *
     * This lib will be include as a compiled dep!
     *
     * => socat/netcat <=> junixsocket <=> PAM authn <=> jline3 <=> LoggingMXBean
     */

    // https://github.com/jline/jline3/wiki/LineReader
    public void interactiveShell()  {
        try {
            Terminal terminal = TerminalBuilder.builder()
                    .system(false)
                    .streams(System.in, System.out)
                    .build();
            terminal.echo(false);
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
            String line = "";
            Boolean loop = true;

            System.out.println("Usage: level [<logger name> <new level>] To Quit type 'quit'");
            System.out.println("Levels: ALL, FINEST, FINER, FINE, CONFIG, INFO, SEVERE, OFF");
            while (loop) {

                try {
                    line = lineReader.readLine("$> ");
                    String[] command = line.split(" ");
                    if (command.length == 1) {
                        if (line.equals("quit")) {
                            loop = false;
                        } else if (line.equals("level")) {
                            System.out.println(listLoggers());
                        } else {
                            System.out.println("error!");
                        }
                    } else if (command.length == 3 ) {
                        Boolean levelCommand = command[0].equals("level");
                        Boolean loggerExists = lbs.getLoggerLevel(command[1].replace("''", "")) != null;
                        Boolean levelExists = command[2] != null && command[2].length() > 0;
                        Boolean failure = true;

                        if (levelCommand && loggerExists && levelExists) {
                            try {
                                lbs.setLoggerLevel(command[1].replace("''", ""), command[2]);
                                failure = false;
                            } catch (Exception e) {
                                failure = true;
                            }
                        }
                        if (failure) {
                            System.out.println("error!");
                        }
                    } else {
                        System.out.println("error!");
                    }
                } catch (UserInterruptException e) {
                    // Ignore
                } catch (EndOfFileException e) {
                    return;
                }
            }
        } catch (IOException e) {

        }
    }

    public String listLoggers() {
        List<String> loggers = lbs.getLoggerNames();
        StringBuilder sb = new StringBuilder();

        loggers.forEach(logger -> {
            String level = lbs.getLoggerLevel(logger);
            Boolean levelIsNotEmpty = level != null && level.length() > 0;
            if (levelIsNotEmpty) {
                sb.append(String.format("Logger '%s' with level '%s' %s", logger, level, System.lineSeparator()));
            }
        });

        return sb.toString();
    }

    /**
     * NOTE! Since this is a library that => no main method!
     * PAM service name, username, and password are
     * Also, NO main method therefore!
     */


    public static void main(String[] args) {
        new Application().interactiveShell();
    }

}
