package modules;

import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

import java.util.LinkedList;

public interface IModule {
    String getModuleTitle(int userID);
    int getModuleCode ();
    LinkedList<BotApiMethod> handle(Update update) throws Throwable;

    LinkedList<BotApiMethod> handleMessage(Update update);
    LinkedList<BotApiMethod> handleCallbackQuery(Update update);

    void initVariables(Update update);

    boolean checkModuleEntering(String messageText);

    SendMessage getModuleMenu();

}
