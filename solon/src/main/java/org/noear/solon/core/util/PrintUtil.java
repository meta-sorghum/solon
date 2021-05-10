package org.noear.solon.core.util;

/**
 * 彩色打印小工具
 *
 * @author noear
 * @since 1.0
 * */
public class PrintUtil {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    public static void blackln(Object txt) {
        System.out.println(ANSI_BLACK + txt);
        System.out.print(ANSI_RESET);
    }

    public static void redln(Object txt) {
        System.out.println(ANSI_RED + txt);
        System.out.print(ANSI_RESET);
    }

    public static void blueln(Object txt) {
        System.out.println(ANSI_BLUE + txt);
        System.out.print(ANSI_RESET);
    }

    public static void greenln(Object txt) {
        System.out.println(ANSI_GREEN + txt);
        System.out.print(ANSI_RESET);
    }

    public static void purpleln(String txt) {
        System.out.println(ANSI_PURPLE + txt);
        System.out.print(ANSI_RESET);
    }

    public static void yellowln(Object txt) {
        System.out.println(ANSI_YELLOW + txt);
        System.out.print(ANSI_RESET);
    }



    public static void debug(Object content) {
        System.out.print("[INFO] ");
        blueln(content);
    }

    public static void debug(String label, Object content) {
        System.out.print("[INFO] ");
        blueln(label + ": " + content);
    }

    public static void info(Object content) {
        System.out.println("[INFO] " + content);
    }

    public static void info(String label, Object content) {
        System.out.print("[INFO] ");
        greenln(label + ": " + content);
    }

    public static void wran(Object content) {
        System.out.print("[INFO] ");
        yellowln(content);
    }

    public static void wran(String label, Object content) {
        System.out.print("[INFO] ");
        yellowln(label + ": " + content);
    }

    public static void error(Object content) {
        System.out.print("[INFO] ");
        redln(content);
    }

    public static void error(String label, Object content) {
        System.out.print("[INFO] ");
        redln(label + ": " + content);
    }
}
