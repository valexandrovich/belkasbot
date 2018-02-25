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
        if (update.hasMessage()) {currentUser = update.getMessage().getFrom();}
        if (update.hasCallbackQuery()){currentUser = update.getCallbackQuery().getFrom();}

        if (!AccountManager.isSessionExist(currentUser.getId())){
            AccountManager.createUser(currentUser);
        }

        ModuleHandler.handle(update);

//        if (update.getMessage().getText().equals("+")) { RatesDB.incrementCursor(update.getMessage().getFrom().getId());}
//        if (update.getMessage().getText().equals("-")) { RatesDB.decrementCursor(update.getMessage().getFrom().getId());}
//        if (update.getMessage().getText().contains("@")) { RatesDB.addUserEmail(update.getMessage().getFrom().getId(), update.getMessage().getText());}
//        System.out.println(RatesDB.getCurrentEmail(update.getMessage().getFrom().getId()));

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
