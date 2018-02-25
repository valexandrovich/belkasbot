package modules;

import configs.BotConfigs;
import database.AccountManager;
import modules.language.LanguageModule;
import modules.rates.RatesModule;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import services.LocalizationService;
import services.LoggerService;

import java.util.LinkedList;

public class ModuleHandler extends TelegramLongPollingBot {
    private static final String LOGTAG = "ModuleHandler";
    private static LinkedList<IModule> modulesList = new LinkedList<>();
    private static LinkedList<String> simpleCommands = new LinkedList<>();

    // Due to singletone
    private ModuleHandler(){};

    static {
        // region Modules creating
        modulesList.add(new RatesModule());
        modulesList.add(new LanguageModule());
        // endregion

        // region Simple Commands creating
        simpleCommands.add("/start");
        simpleCommands.add("/stop");
        simpleCommands.add("/help");
        // endregion
    }

    public static void handle(Update update){
        int userID = 0;
        long chatID = 0;
        String messageText = "";
        String callbackData = "";
        int userState = 0;
        if (update.hasMessage()) {
            userID = update.getMessage().getFrom().getId();
            chatID = update.getMessage().getChatId();
            messageText = update.getMessage().getText();
        }
        else if (update.hasCallbackQuery()){
            userID = update.getCallbackQuery().getFrom().getId();
            chatID = update.getCallbackQuery().getMessage().getChatId();
            callbackData = update.getCallbackQuery().getData();
        }

        // Checking on Simple Command
        if (messageText.length()>0 && simpleCommands.contains(messageText)){
            try {
                handleSimpleCommand(userID, chatID, messageText);
            } catch (TelegramApiException e) {
                LoggerService.logError(LOGTAG, e);
            }
            return;
        }

        // Checking on Main Menu call
        if (messageText.length()>0 && isMainMenuCall(messageText, userID)){
            sendMainMenu(userID, chatID);
            return;
        }

        // Checking message is module title
        if (messageText.length()>0){
            checkModuleEntering(messageText, userID);
        }

        userState = AccountManager.getUserState(userID);
        boolean findModule = false;
        for (IModule module : modulesList){
            if (userState == module.getModuleCode()){
                try{
                    module.handle(update);
                }
                catch (Throwable e){
                    // Catching errors from module
                    LoggerService.logError(LOGTAG, e);
                    sendMainMenu(userID, chatID);
                }
                findModule = true;
                LoggerService.logInfo(LOGTAG, "Find module - " + module.getModuleTitle(userID));
            }
        }
        if (!findModule){
            LoggerService.logInfo(LOGTAG, "Cant find module");
            sendMainMenu(userID, chatID);
        }



    }

    // region Main menu handler
    public static boolean isMainMenuCall(String message, int userID){
        if (message.equals(LocalizationService.getString("simpleCommandMainMenu", userID))){
            return true;
        } else {
            return false;
        }
    }
    public static void sendMainMenu(int userID, long chatID){
        if (AccountManager.setUserState(userID, 0)) {
            SendMessage sendMessage = new SendMessage()
                    .setChatId(chatID)
                    .setText(LocalizationService.getString("simpleCommandStart", userID));

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            LinkedList<KeyboardRow> rows = new LinkedList<>();

            for (IModule module : modulesList) {
                KeyboardRow row = new KeyboardRow();
                row.add(module.getModuleTitle(userID));
                rows.add(row);
            }
            replyKeyboardMarkup.setKeyboard(rows);
            replyKeyboardMarkup.setResizeKeyboard(true);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
            try {
                LoggerService.logInfo(LOGTAG, "Sending main menu");
                new ModuleHandler().execute(sendMessage);
            } catch (TelegramApiException e) {
                LoggerService.logError(LOGTAG, e);
            }
        }
    }
    // endregion

    // region Simple command handler
    private static void handleSimpleCommand(int userID, long chatID, String message) throws TelegramApiException {

        SendMessage sendMessage = new SendMessage()
                .setChatId(chatID);

        switch (message.toLowerCase()){
            case "/start":{
                sendMainMenu(userID, chatID);
                return;
            }
            case "/stop":{
                sendMessage.setText(LocalizationService.getString("simpleCommandStop", userID));
                sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
                AccountManager.setUserState(userID, 0);
                break;
            }
            case "/help":{
                sendMessage.setText(LocalizationService.getString("simpleCommandHelp", userID));
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                LinkedList<KeyboardRow> rows = new LinkedList<>();
                KeyboardRow row = new KeyboardRow();
                row.add(LocalizationService.getString("simpleCommandMainMenu", userID));
                rows.add(row);
                replyKeyboardMarkup.setKeyboard(rows);
                replyKeyboardMarkup.setResizeKeyboard(true);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                AccountManager.setUserState(userID, 0);
                break;
            }
            default:{
                break;
            }
        }
        new ModuleHandler().execute(sendMessage);
        return;
    }
    // endregion

    // region Cheking module title
    private static void checkModuleEntering(String message, int userID){
        for (IModule module : modulesList){
            if (message.equals(LocalizationService.getString(module.getModuleCode()+"_title", userID))){
              if (AccountManager.setUserState(userID, module.getModuleCode())){
                    return;
              }
            }
        }
    }
    // endregion

    // region Bot method override
    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public String getBotUsername() {
        return BotConfigs.getBotName();
    }

    @Override
    public String getBotToken() {
        return BotConfigs.getBotToken();
    }
    // endregion



}
