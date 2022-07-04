package com.horcruxid.main.com.horcruxid.main;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.LoggingMXBean;

public class TestSuite {
    private static Logger log = Logger.getLogger(TestSuite.class.getName());
    private static MBeanServer mbs;
    private static LoggingMXBean lbs;

    @BeforeAll
    static void setup() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        // LEVELS: ALL, FINEST, FINER, FINE, CONFIG, INFO, SEVERE, OFF
        mbs = ManagementFactory.getPlatformMBeanServer();
        lbs = LogManager.getLoggingMXBean();
    }
    
    void listLoggers() {
        List<String> loggers = lbs.getLoggerNames();
        loggers.forEach(logger -> {
            String level = lbs.getLoggerLevel(logger);
            Boolean levelIsNotEmpty = level != null && level.length() > 0;
            if (levelIsNotEmpty) {
                log.info(String.format("Logger '%s' => '%s'", logger.toString(), level));
            }
        });
    }

    @Test
    void testChangeLevel() {
        lbs.setLoggerLevel("org.junit", "ALL"); // was WARNING
        listLoggers();
    }
}
