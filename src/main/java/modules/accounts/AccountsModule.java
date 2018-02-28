package modules.accounts;

import configs.BotConfigs;
import database.AccountManager;
import modules.IModule;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import services.LocalizationService;
import services.LoggerService;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AccountsModule extends TelegramLongPollingBot implements IModule {
    private final String LOGTAG = "AccountsModule";
    private final Object lock = new Object();


    @Override
    public String getModuleTitle(int userID) {
        return LocalizationService.getString(getModuleCode()+"_Title", userID);
    }

    @Override
    public int getModuleCode() {
        return 2;
    }

    @Override
    public void handle(Update update) throws Throwable {
        synchronized (lock) {
            if (update.hasMessage()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        }
    }

    @Override
    public void handleMessage(Message message) throws TelegramApiException, IOException {
        int userID = message.getFrom().getId();
        long chatID = message.getChatId();

        SendMessage sendMessage = new SendMessage()
                .setChatId(chatID);
        try{
            if (checkModuleEnering(message.getText(), userID)){
                sendMessage.setText(LocalizationService.getString("2_Welcome", userID))
                        .setReplyMarkup(getModuleMenu(userID));
                execute(sendMessage);
            } else {

                AccountsDB.createAccountsUserCursor(userID);

                try {
                    AccountsDB.setAccountUserCursor(userID, AccountsDB.findIndex(message.getText(), userID));
                    sendMessage.setText(AccountsDB.getCurrentRecord(userID))
                            .setReplyMarkup(getAccountsMenu(userID));
                    execute(sendMessage);
                }
                catch (AccountsException e){
                    sendMessage.setText(LocalizationService.getString("2_AccountNotExist", userID))
                            .setReplyMarkup(getModuleMenu(userID));
                    execute(sendMessage);
                }
            }

        } catch (Throwable throwable){
            LoggerService.logError(LOGTAG, throwable);
        }
    }

    @Override
    public void handleCallbackQuery(CallbackQuery callbackQuery) throws Throwable {
        int userID = callbackQuery.getFrom().getId();
        long chatID = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();

        EditMessageText editMessageText = new EditMessageText()
                .setMessageId(callbackQuery.getMessage().getMessageId())
                .setReplyMarkup(getAccountsMenu(userID))
                .setChatId(chatID);
        String indexInMessage = callbackQuery.getMessage().getText()
                .replace(LocalizationService.getString("2_AccountPrefix", userID), "")
                .trim()
                .substring(0, 4);
        AccountsDB.setAccountUserCursor(userID, AccountsService.getPointerByAccount(indexInMessage));
        switch (callbackData){

            case "+":{

                AccountsDB.setAccountUserCursor(userID, AccountsDB.getAccountsUserCursor(userID) + 1);
                editMessageText.setText(AccountsDB.getCurrentRecord(userID))
                        .setReplyMarkup(getAccountsMenu(userID));
                execute(editMessageText);
                return;
            }
            case "-":{
                AccountsDB.setAccountUserCursor(userID, AccountsDB.getAccountsUserCursor(userID) - 1);
                editMessageText.setText(AccountsDB.getCurrentRecord(userID))
                        .setReplyMarkup(getAccountsMenu(userID));;
                execute(editMessageText);
                return;
            }
        }

    }

    @Override
    public boolean checkModuleEnering(String messageText, int userID) {
        boolean moduleEntering = false;
        if (messageText.equals(LocalizationService.getString(getModuleCode()+"_Title", userID))){
            moduleEntering = true;
        }
        return moduleEntering;
    }

    @Override
    public ReplyKeyboardMarkup getModuleMenu(int userID) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        LinkedList<KeyboardRow> rows = new LinkedList<>();

//        KeyboardRow allPlanRow = new KeyboardRow();
//        allPlanRow.add(LocalizationService.getString("2_AllPlan", userID));
//        rows.add(allPlanRow);

        KeyboardRow rowMainMenuRow = new KeyboardRow();
        rowMainMenuRow.add(LocalizationService.getString("SimpleCommandMainMenu", userID));
        rows.add(rowMainMenuRow);

        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup getAccountsMenu(int userID){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new LinkedList<>();

        int currentCursor = AccountsDB.getAccountsUserCursor(userID);
        String nextAccount = AccountsService.getAccountByPointer(currentCursor + 1);
        String previousAccount = AccountsService.getAccountByPointer(currentCursor -1);


        List<InlineKeyboardButton> row1 = new LinkedList<>();
        if (previousAccount!=null) {
            row1.add(new InlineKeyboardButton().setText(LocalizationService.getString("2_PreviousAccount", userID) + " " + previousAccount).setCallbackData("-"));
        }
        if (nextAccount!=null) {
            row1.add(new InlineKeyboardButton().setText(nextAccount +" "  + LocalizationService.getString("2_NextAccount", userID)).setCallbackData("+"));
        }

        rows.add(row1);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    // region Bot Overrides Methods

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
