package net.katch0420.macebot.main.messenger;

import net.katch0420.macebot.main.MaceBot;
import net.minecraft.text.Text;
import org.slf4j.Marker;

import static net.katch0420.macebot.main.MaceBot.LOGGER;

public class Logger {

    public static void warn(Text msg) {
        warn(msg.toString());
    }

    public static void warn(String msg) {
        LOGGER.warn(msg);
    }

    public static void error(Text msg) {
        error(msg.toString());
    }

    public static void error(String msg) {
        LOGGER.error(msg);
    }

    public static void info(Text msg) {
        info(msg.toString());
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void debug(String msg, Object o, boolean b) {
        if (b && MaceBot.debugMode) LOGGER.debug(msg, o.getClass());
    }

    public static void debug(String msg, Class<?> c, boolean b) {
        if (b && MaceBot.debugMode) LOGGER.debug(msg, c);
    }
}
