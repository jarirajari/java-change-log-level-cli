package com.horcruxid.main;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXServerSocketChannel;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import javax.management.MBeanServer;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
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

    /*
     * https://kohlschutter.github.io/junixsocket/junixsocket-demo/xref/index.html
     *
     * https://github.com/jline/jline3/wiki/LineReader
     */

    public void interactiveShell(InputStream in, OutputStream out)  {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            Terminal terminal = TerminalBuilder.builder()
                    .system(false)
                    .streams(in, out)
                    .build();
            terminal.echo(false);
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
            String line = "";
            Boolean loop = true;

            writer.write("Usage: level [<logger name> <new level>] To Quit type 'quit'\n");
            writer.write("Levels: ALL, FINEST, FINER, FINE, CONFIG, INFO, SEVERE, OFF\n");
            writer.flush();
            while (loop) {

                try {
                    line = lineReader.readLine("$> ");

                    String[] command = line.split(" ");
                    if (command.length == 1) {
                        if (line.equals("quit")) {
                            loop = false;
                        } else if (line.equals("level")) {
                            writer.write(listLoggers());
                            writer.flush();
                        } else {
                            writer.write("error!\n");
                            writer.flush();
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
                            writer.write("error!\n");
                            writer.flush();
                        }
                    } else {
                        writer.write("error!\n");
                        writer.flush();
                    }
                } catch (UserInterruptException e) {
                    // Ignore
                } catch (EndOfFileException e) {
                    // ignore
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String listLoggers() {
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
     * NOTE! Since this is a library that => no main method! Only for testing...
     * UNIX domain socket communication: https://blog.travismclarke.com/post/socat-tutorial/
     * "socat UNIX-CONNECT:/tmp/change-log.sock -"
     */
    public static void main(String[] args) {
        String userId = (args.length == 1) ? args[0] : "";

        new SocketServer().start(userId);
    }


}
