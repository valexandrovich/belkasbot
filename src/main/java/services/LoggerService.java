package services;

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



    // 1 - All
    // 2 - Actions
    // 3 - Errors
    // 4 - Only print stacktrace

    private static final int logLevel = 1;


    public static void logError(String logtag, Throwable throwable){
        //BotLogger.error(logtag, throwable.getMessage());
        if (logLevel== 4){
            throwable.printStackTrace();
        } else {
            log(logtag, throwable.getMessage(), 3);
        }


    }

    public static void logAction(String logtag, String message){
        log(logtag, message, 2);
    }

    public static void logInfo(String logtag, String message){
        log(logtag, message, 1);
    }



    private static void log(String logtag, String message, int logPriority){
        String color;
        switch (logPriority){
            case 1 : color = ANSI_GREEN; break;
            case 2 : color = ANSI_BLUE; break;
            case 3 : color = ANSI_RED; break;
            default: color = ANSI_BLACK; break;
        }

        if (logPriority>=logLevel){
            System.out.println(color + "["+ logtag+"] : "+message + ANSI_RESET);
        }


    }
}
