package modules.language;

import configs.BotConfigs;
import database.AccountManager;
import modules.ModuleHandler;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import services.LocalizationService;
import services.LoggerService;

import java.util.LinkedList;
import java.util.List;

public class LanguageModule extends TelegramLongPollingBot implements IModule {

    private final static String LOGTAG = "LanguageModule";
    private List<LocalizationService.Language> languageList;

    @Override
    public String getModuleTitle(int userID) {
        return LocalizationService.getString(getModuleCode()+"_Title", userID);
    }

    @Override
    public int getModuleCode() {
        return 91;
    }

    @Override
    public void handle(Update update) throws Throwable {
        if (update.hasMessage()){
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()){
            handleCallbackQuery(update.getCallbackQuery());
        } else {
            LoggerService.logError(LOGTAG, new Throwable(LOGTAG + "can't reconize update type"));
        }
    }

    public void handleMessage(Message message) throws TelegramApiException {
        int userID = message.getFrom().getId();
        long chatID = message.getChatId();
        SendMessage sendMessage = new SendMessage()
                .setChatId(chatID);
        if (checkModuleEnering(message.getText(), userID)){
            sendMessage.setText(LocalizationService.getString(getModuleCode()+"_Welcome", userID))
                    .setReplyMarkup(getModuleMenu(userID));
            execute(sendMessage);

        } else {

            for (LocalizationService.Language language : languageList){
                if (message.getText().equals(language.toString())){
                    if (AccountManager.setUserLanguage(userID, language.getCode())){
                        ModuleHandler.sendMainMenu(userID, chatID);
                        return;
                    }
                }
            }



        }

    }
    public void handleCallbackQuery(CallbackQuery callbackQuery) throws Throwable {
        throw new Throwable("Can't handle CallbackQuery");
    }

    @Override
    public boolean checkModuleEnering(String messageText, int userID) {
        boolean moduleEntering = false;
        if (messageText.equals(LocalizationService.getString(getModuleCode()+"_Title", userID))){
            moduleEntering = true;
        }
        return moduleEntering;
    }
    public  ReplyKeyboardMarkup getModuleMenu(int userID){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        LinkedList<KeyboardRow> rows = new LinkedList<>();
        languageList = LocalizationService.getSupportedLanguages();
        for (LocalizationService.Language language : languageList){
                KeyboardRow row = new KeyboardRow();
                row.add(language.toString());
                rows.add(row);
        }
        KeyboardRow row = new KeyboardRow();
        row.add(LocalizationService.getString("SimpleCommandMainMenu", userID));
        rows.add(row);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;


    }

    // region Override Bot Methods
    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("Язык модуль тоже получил апдейт!! :(  ");
    }

    @Override
    public String getBotUsername() {
        return BotConfigs.getBotName();
    }

    @Override
    public String getBotToken() {
        return BotConfigs.getBotToken();
    }

    //endregion
}
