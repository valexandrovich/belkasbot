package services;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerService {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private static final SimpleDateFormat shortDate = new SimpleDateFormat("dd.MM.yyyy");

    private static boolean printStackTrace = true;
    public static void setPrintStackTrace(boolean printStackTrace) {
        LoggerService.printStackTrace = printStackTrace;
    }

    public static void logError (String logtag, String message){
        log(logtag, message, 1);
    }
    public static void logError (String logtag, Throwable throwable){
        log(logtag, throwable.getMessage(), 1);
        if (printStackTrace){
            throwable.printStackTrace();
        }
    }
    public static void logAction (String logtag, String message){
        log(logtag, message, 2);
    }
    public static void logInfo (String logtag, String message){
        log(logtag, message, 3);
    }

    private static void log(String logtag, String message, int color){
        String ansi_color;
        switch (color){
            case 1 : ansi_color = ANSI_RED; break;
            case 2 : ansi_color = ANSI_GREEN; break;
            case 3 : ansi_color = ANSI_BLUE; break;
            default: ansi_color = ANSI_BLACK; break;
        }

        Date now = new Date(System.currentTimeMillis());

        String fullMessage = ansi_color
                + "[" + shortDate.format(now) + "]"
                + "[" + logtag + "]"
                + message
                + ANSI_RESET;


        System.out.println(fullMessage);


    }



}
