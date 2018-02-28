import handlers.BelkaBot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.logging.BotLogger;

public class BotStarter {

    final private static String LOGTAG = "BotStarter";

    public static void main(String[] args) {

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new BelkaBot());
        } catch (TelegramApiRequestException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
