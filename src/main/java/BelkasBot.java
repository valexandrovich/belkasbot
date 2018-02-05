import controller.ConfigsHandler;
import controller.ModuleHandler;
import modules.BotModule;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class BelkasBot extends TelegramLongPollingBot {

    public static Properties botProperties = new Properties();
    private static ModuleHandler moduleHandler;

    public static void main(String[] args) {
        // Loading Configs
        ConfigsHandler.loadBotConfigs(botProperties);
        moduleHandler = new ModuleHandler();

        // Create and register Belkas Bot
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new BelkasBot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = moduleHandler.getResponse(update);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return null;
    }

    @Override
    public String getBotToken() {
        return botProperties.getProperty("BOT_TOKEN");
    }
}
