package modules.language;

import database.AccountManager;
import modules.IModule;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import services.LocalizationService;
import services.LoggerService;
import services.SenderService;
import services.SimpleCommandService;

import java.util.LinkedList;
import java.util.List;

public class LanguageModule implements IModule {

    List<LocalizationService.Language> languages = LocalizationService.getSupportedLanguages();

    private final static String LOGTAG = "LanguageModule";
    long callBackMessageID;
    long chatID;
    int userID;
    String userName;
    String moduleTitle;
    String messageText;


    @Override
    public String getModuleTitle(int userID) {
        return LocalizationService.getString(getModuleCode()+"_Title", userID);
    }
    @Override
    public int getModuleCode() {
        return 91;
    }

    @Override
    public LinkedList<BotApiMethod> handle(Update update) throws Throwable {
        initVariables(update);
        if (update.hasMessage()){
            return handleMessage(update);
        } else if (update.hasCallbackQuery()){
            return handleCallbackQuery(update);
        } else {
            throw new Throwable(LOGTAG + " : Update hasn't message or callbackquery");
        }
    }

    @Override
    public LinkedList<BotApiMethod> handleMessage(Update update) {
        LinkedList<BotApiMethod> responseList = new LinkedList<>();
        if (checkModuleEntering(messageText)){
            responseList.add(getModuleMenu());
            return responseList;
        } else {
            boolean findLanguage = false;
            for (LocalizationService.Language language : languages){
                if (messageText.equals(language.toString())){
                    AccountManager.setUserLanguage(userID, language.getCode());
                    AccountManager.setUserState(userID, 0);
                    findLanguage = true;
                    responseList.add(SimpleCommandService.getMainMenu(userID, chatID));
                    return responseList;
                }
            }

            SendMessage sendMessage = new SendMessage()
                    .setChatId(chatID)
                    .setText(LocalizationService.getString(getModuleCode()+"_BadCommand", userID));
            responseList.add(sendMessage);
            return responseList;


        }
    }

    @Override
    public LinkedList<BotApiMethod> handleCallbackQuery(Update update) {
        return null;
    }

    @Override
    public void initVariables(Update update) {
        if (update.hasMessage()){
            chatID = update.getMessage().getChatId();
            userID = update.getMessage().getFrom().getId();
            userName = AccountManager.getUserName(userID);
            moduleTitle = getModuleTitle(userID);
            messageText = update.getMessage().getText();
        } else if (update.hasCallbackQuery()){
            chatID = update.getCallbackQuery().getMessage().getChatId();
            userID = update.getCallbackQuery().getFrom().getId();
            callBackMessageID = update.getCallbackQuery().getMessage().getMessageId();
            userName = AccountManager.getUserName(userID);
            moduleTitle = getModuleTitle(userID);
            messageText = update.getCallbackQuery().getMessage().getText();
        }
    }

    @Override
    public boolean checkModuleEntering(String messageText) {
        if (messageText.equals(moduleTitle)){
            LoggerService.logInfo(LOGTAG, userName + " : detected entering in module" + moduleTitle);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public SendMessage getModuleMenu(){
        SendMessage sendMessage = new SendMessage()
                .setChatId(chatID)
                .setText(LocalizationService.getString(getModuleCode()+"_Welcome", userID));

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new LinkedList<>();
        for (LocalizationService.Language language : languages){
            KeyboardRow row = new KeyboardRow();
            row.add(language.toString());
            rows.add(row);
        }
        rows.add(SimpleCommandService.getMainMenuRow(userID));

        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(rows);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }
}
