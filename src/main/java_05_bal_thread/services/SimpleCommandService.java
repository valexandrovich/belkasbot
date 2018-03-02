package services;

import database.AccountManager;
import modules.IModule;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.LinkedList;

public class SimpleCommandService {



    public static LinkedList<BotApiMethod> handleSimpleCommand(String command, long chatID, int userID){

        LinkedList<BotApiMethod> responseList = new LinkedList<>();

        SendMessage sendMessage = new SendMessage()
                .setChatId(chatID);

        switch (command){
            case "/start":{
                sendMessage = getMainMenu(userID, chatID);
                AccountManager.setUserState(userID, 0);
                break;
            }
            case "/stop":{
                sendMessage.setText(LocalizationService.getString("SimpleCommandStop", userID));
                sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
                AccountManager.setUserState(userID, 0);
                break;
            }
            case "/help":{
                sendMessage.setText(LocalizationService.getString("SimpleCommandHelp", userID));
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                LinkedList<KeyboardRow> rows = new LinkedList<>();
                rows.add(getMainMenuRow(userID));
                replyKeyboardMarkup.setKeyboard(rows);
                replyKeyboardMarkup.setResizeKeyboard(true);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                AccountManager.setUserState(userID, 0);
                break;
            }
            default:{
                sendMessage = getMainMenu(userID, chatID);
                AccountManager.setUserState(userID, 0);
                break;
            }
        }
        responseList.add(sendMessage);
        return responseList;
    }

    public static SendMessage getMainMenu(int userID, long chatID){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(LocalizationService.getString("SimpleCommandStart", userID));
        sendMessage.setChatId(chatID);

        LinkedList<IModule> modules = SenderService.getModulesList();
        ReplyKeyboardMarkup mainMenuKeyboard = new ReplyKeyboardMarkup();
        LinkedList<KeyboardRow> rows = new LinkedList<>();
        for (IModule module : modules){
            KeyboardRow row = new KeyboardRow();
            row.add(module.getModuleTitle(userID));
            rows.add(row);
        }
        mainMenuKeyboard.setKeyboard(rows);
        mainMenuKeyboard.setResizeKeyboard(true);
        sendMessage.setReplyMarkup(mainMenuKeyboard);
        return sendMessage;
    }

    public static KeyboardRow getMainMenuRow(int userID){
        KeyboardRow row = new KeyboardRow();
        row.add(LocalizationService.getString("SimpleCommandMainMenu", userID));
        return row;
    }

    public static boolean isSimpleCommand(String command, int userID){
        String mainMenuTitle = LocalizationService.getString("SimpleCommandMainMenu", userID);
        if (command.equals("/start")
                || command.equals("/stop")
                || command.equals("/help")
                || command.equals(mainMenuTitle)){
            return true;
        } else {
            return false;
        }
    }

}
