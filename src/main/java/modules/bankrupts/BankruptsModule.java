package modules.bankrupts;

import configs.BotConfigs;
import modules.IModule;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import services.LocalizationService;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

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
        } else {
            try {
                LinkedList<BankruptRow> response = BankruptsService.findByCode(message.getText());
                if (response != null && response.size()>0){
                    BankruptsBD.clearUserSession(message.getFrom().getId());
                    BankruptsBD.addUserSession(response, message.getFrom().getId());

                    BankruptRow row = BankruptsBD.getCurrentBankruptRow(userID);
                    sendMessage.setParseMode("HTML");
                    sendMessage.setText(rowToMessage(row, userID));
                    sendMessage.setReplyMarkup(getBankruptsButtons(userID));
                    execute(sendMessage);

                } else {
                    sendMessage.setText(LocalizationService.getString("3_NotExist", userID));
                    execute(sendMessage);
                    sendMessage.setText(LocalizationService.getString("3_Welcome", userID));
                    execute(sendMessage);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleCallbackQuery(CallbackQuery callbackQuery) throws Throwable {
        String data = callbackQuery.getData();
        int userID = callbackQuery.getFrom().getId();
        int cursor = BankruptsBD.getUserPointer(userID);
        int messageID = callbackQuery.getMessage().getMessageId();
        long chatID = callbackQuery.getMessage().getChatId();
        switch (data){
            case "next":{
                if(BankruptsBD.setUserPointer(userID, cursor+1)) {
                    EditMessageText editMessageText = new EditMessageText()
                            .setChatId(chatID)
                            .setMessageId(messageID)
                            .setReplyMarkup(getBankruptsButtons(userID))
                            .setParseMode("HTML")
                            .setText(rowToMessage(BankruptsBD.getCurrentBankruptRow(userID), userID));
                    execute(editMessageText);
                }

                break;
            }
            case "previous":{
                if(BankruptsBD.setUserPointer(userID, cursor-1)) {
                    EditMessageText editMessageText = new EditMessageText()
                            .setChatId(chatID)
                            .setMessageId(messageID)
                            .setReplyMarkup(getBankruptsButtons(userID))
                            .setParseMode("HTML")
                            .setText(rowToMessage(BankruptsBD.getCurrentBankruptRow(userID), userID));
                    execute(editMessageText);
                }
                break;
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

        KeyboardRow rowMainMenuRow = new KeyboardRow();
        rowMainMenuRow.add(LocalizationService.getString("SimpleCommandMainMenu", userID));
        rows.add(rowMainMenuRow);

        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;
    }

    private InlineKeyboardMarkup getBankruptsButtons(int userID){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new LinkedList<>();

        int previous = BankruptsBD.getUserPointer(userID);
        int next = BankruptsBD.getSessionRowsCount(userID) - previous -1;

        LinkedList<InlineKeyboardButton> row1 = new LinkedList<>();

        if (previous>0) {
            row1.add(new InlineKeyboardButton().setText("< " + previous).setCallbackData("previous"));
        }
        if (next>0){
            row1.add(new InlineKeyboardButton().setText(next + " >").setCallbackData("next"));
        }

        rows.add(row1);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;



    }

    private String rowToMessage(BankruptRow row, int userID){


        String answer = "";
        answer = "<b>" + LocalizationService.getString("3_PublicDate", userID) + " : </b>"+  row.getPublicDate() + "\n" +
                "<b>" +LocalizationService.getString("3_OKPO", userID) + " : </b>"+ row.getOkpo() + "\n"+
                "<b>" +LocalizationService.getString("3_Name", userID) + " : </b>"+ row.getName() + "\n"+
                "<b>" +LocalizationService.getString("3_DocType", userID) + " : </b>"+ row.getDocType() + "\n"+
                "<b>" +LocalizationService.getString("3_DocNumber", userID) + " : </b>"+ row.getDocNumber() + "\n"+
                "<b>" +LocalizationService.getString("3_CourtName", userID) + " : </b>"+ row.getCourtName();
        return answer;

    }

    private String nvl(String text){
        if (text == null || text.length()<1){
            return "";
        } else {
            return text;
        }
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
