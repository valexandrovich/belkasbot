package handlers;

import configs.BotConfigs;
import database.AccountManager;
import modules.ModuleHandler;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public class BelkaBot extends TelegramLongPollingBot {

    private User currentUser;

    @Override
    public void onUpdateReceived(Update update) {
        long chatID = 0;
        if (update.hasMessage()) {
            currentUser = update.getMessage().getFrom();
            chatID = update.getMessage().getChatId();
        }
        if (update.hasCallbackQuery()){
            currentUser = update.getCallbackQuery().getFrom();
            chatID = update.getCallbackQuery().getMessage().getChatId();
        }

        if (!AccountManager.isSessionExist(currentUser.getId())){
            AccountManager.createSession(currentUser, chatID);
        }

        ModuleHandler.handle(update);


    }

    @Override
    public String getBotUsername() {
        return BotConfigs.getBotName();
    }

    @Override
    public String getBotToken() {
        return BotConfigs.getBotToken();
    }
}
