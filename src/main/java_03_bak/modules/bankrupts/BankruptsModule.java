package modules.bankrupts;

import configs.BotConfigs;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import services.LocalizationService;

import java.io.IOException;
import java.util.LinkedList;

public class BankruptsModule extends TelegramLongPollingBot implements IModule {

    private final String LOGTAG = "BankruptsModule";
    private final Object lock = new Object();
    @Override
    public String getModuleTitle(int userID) {
        return LocalizationService.getString("3_Title", userID);
    }

    @Override
    public int getModuleCode() {
        return 3;
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

        if (checkModuleEnering(message.getText(), userID)){
            sendMessage.setText(LocalizationService.getString("3_Welcome", userID))
                    .setReplyMarkup(getModuleMenu(userID));
            execute(sendMessage);
        }
    }

    @Override
    public void handleCallbackQuery(CallbackQuery callbackQuery) throws Throwable {

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

        KeyboardRow rowMainMenuRow = new KeyboardRow();
        rowMainMenuRow.add(LocalizationService.getString("SimpleCommandMainMenu", userID));
        rows.add(rowMainMenuRow);

        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;
    }


    // region Bot Method Overrite
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
