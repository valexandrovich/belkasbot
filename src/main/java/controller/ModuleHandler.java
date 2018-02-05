package controller;

import modules.BotModule;
import modules.RatesModule;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.LinkedList;
import java.util.List;

public class ModuleHandler {

    private List<BotModule> modulesList = new LinkedList<>();
    private BotModule currentModule;
    ReplyKeyboardMarkup mainMenu;
    ReplyKeyboardMarkup exitMenu;

    public void initModules() {
        modulesList.add(new RatesModule());
    }

    public ModuleHandler() {
        initModules();

        mainMenu = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new LinkedList<>();
        for (BotModule module : modulesList) {
            KeyboardRow row = new KeyboardRow();
            row.add(module.getAction());
            keyboardRows.add(row);
        }
        mainMenu.setResizeKeyboard(true);
        mainMenu.setKeyboard(keyboardRows);

        exitMenu = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows1 = new LinkedList<>();
        for (BotModule module : modulesList) {
            KeyboardRow row = new KeyboardRow();
            row.add("В главное меню");
            keyboardRows1.add(row);
        }
        exitMenu.setResizeKeyboard(true);
        exitMenu.setKeyboard(keyboardRows1);

    }

    public SendMessage getResponse(Update getUpdate) {
        SendMessage sendMessage = new SendMessage();

        if (getUpdate.hasCallbackQuery()) {
            sendMessage.setChatId(getUpdate.getCallbackQuery().getMessage().getChatId());
        }
        if (getUpdate.hasMessage()) {
            sendMessage.setChatId(getUpdate.getMessage().getChatId());
        }


        if (getUpdate.hasMessage() && getUpdate.getMessage().getText().equals("/start")) {
            System.out.println("Sending main menu");
            sendMessage.setText("Выберите действие");
            sendMessage.setReplyMarkup(mainMenu);
            return sendMessage;
        } else if (getUpdate.hasMessage() && getUpdate.getMessage().getText().equals("В главное меню")) {
            currentModule = null;
            System.out.println("Sending main menu");
            sendMessage.setReplyMarkup(mainMenu);
            sendMessage.setText("Выберите действие");
            return sendMessage;
        }

        if (currentModule == null){
            System.out.println("No current module. Searching...");
            for (BotModule module : modulesList){
                if (module.acceptUpdate(getUpdate.getMessage().getText())){
                    currentModule = module;
                    System.out.println("Find module - " + module.getClass().getName());
                } else {
                    System.out.println("No suitable module");
                    sendMessage.setChatId(getUpdate.getMessage().getChatId());
                    sendMessage.setText("Команда не найдена\nВыберите действие");
                    sendMessage.setReplyMarkup(mainMenu);
                    return sendMessage;
                }
            }
        }

        System.out.println("Sending request to "+currentModule.getClass().getName()+" module");
        //sendMessage.setReplyMarkup(exitMenu);
        sendMessage = currentModule.getResponce(getUpdate);
        System.out.println("Going to return message from ModuleHandler " + sendMessage.getText() );
        return sendMessage;





    }
}





