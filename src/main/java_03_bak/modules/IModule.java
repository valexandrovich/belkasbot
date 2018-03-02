package modules;

import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.IOException;

public interface IModule {
    String getModuleTitle(int userID);
    int getModuleCode();

    void handle(Update update) throws Throwable;

    void handleMessage(Message message) throws TelegramApiException, IOException;
    void handleCallbackQuery(CallbackQuery callbackQuery) throws Throwable;

    boolean checkModuleEnering(String messageText, int userID);
    ReplyKeyboardMarkup getModuleMenu(int userID);

}
