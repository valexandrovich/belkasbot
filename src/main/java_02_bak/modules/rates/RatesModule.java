package modules.rates;

import configs.BotConfigs;
import database.AccountManager;
import org.telegram.telegrambots.api.methods.send.SendDocument;
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
import services.LoggerService;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RatesModule extends TelegramLongPollingBot implements IModule {
    private static final String LOGTAG = "RatesModule";
    private static final int WIDTH_INLINE_BUTTON = 25;
    private LinkedList<String> moduleCommands = new LinkedList<>();

    public RatesModule(){
        moduleCommands.add(getModuleCode()+"_RatesActual");
        moduleCommands.add(getModuleCode()+"_RatesMonth");
    }

    @Override
    public String getModuleTitle(int userID) {
        return LocalizationService.getString(getModuleCode()+"_title", userID);
    }

    @Override
    public int getModuleCode() {
        return 1;
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

    public void handleMessage(Message message) throws TelegramApiException, IOException {
        int userID = message.getFrom().getId();
        long chatID = message.getChatId();
        SendMessage sendMessage = new SendMessage()
                .setChatId(chatID);
        if (message.getText().equals(getModuleTitle(userID))){
            sendMessage.setReplyMarkup(getModuleMenu(userID))
                    .setText(LocalizationService.getString(getModuleCode()+"_Welcome", userID));
            execute(sendMessage);
            return;
        } else if (message.getText().equals(LocalizationService.getString("1_RatesActual", userID))){
            sendMessage.setText(RatesService.getRatesMessage(new Date(System.currentTimeMillis()), userID));
            sendMessage.setReplyMarkup(getRatesMessageMenu(userID));
            execute(sendMessage);
            RatesDB.addRatesResponce(userID, new Timestamp(System.currentTimeMillis()));
            return;
        } else if (message.getText().equals(LocalizationService.getString("1_RatesMonth", userID))){
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            sendMessage.setText(RatesService.getRatesMessage(new Date(calendar.getTimeInMillis()), userID));
            sendMessage.setReplyMarkup(getRatesMessageMenu(userID));
            execute(sendMessage);
            RatesDB.addRatesResponce(userID, new Timestamp(calendar.getTimeInMillis()));
            return;
        } else if (message.getText().contains("@")&& message.getText().contains(".") ){
            if (RatesDB.addUserEmail(userID, message.getText())){
                sendMessage.setText(LocalizationService.getString("1_EmailAdded", userID));
                execute(sendMessage);
                sendMessage.setText(RatesService.getRatesMessage(RatesDB.getLastResponseDate(userID), userID))
                        .setReplyMarkup(getRatesMessageMenu(userID));
                execute(sendMessage);
            }
        } else {
            String date = message.getText();
            date.trim().replace(",", ".").replace(" ", "");

            try {
                Date rateDate = new SimpleDateFormat("dd.MM.yyyy").parse(date);
                sendMessage.setText(RatesService.getRatesMessage(rateDate, userID));
                sendMessage.setReplyMarkup(getRatesMessageMenu(userID));
            } catch (ParseException e) {
                sendMessage.setText(LocalizationService.getString("1_parseDateError", userID));
            }
            execute(sendMessage);
        }
    }
    public void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        int userID = callbackQuery.getFrom().getId();
        long chatID = callbackQuery.getMessage().getChatId();
        long messageID = callbackQuery.getMessage().getMessageId();
        String callbackData = callbackQuery.getData();
        SendMessage sendMessage = new SendMessage()
                .setChatId(chatID);
        if (AccountManager.setLastCallbackMessageID(userID, messageID)){
            switch (callbackData){
                case "previousEmail":{
                    if (RatesDB.incrementCursor(userID)){
                        EditMessageText editMessageText = new EditMessageText()
                                .setMessageId(callbackQuery.getMessage().getMessageId())
                                .setText(callbackQuery.getMessage().getText())
                                .setChatId(callbackQuery.getMessage().getChatId())
                                .setReplyMarkup(getSettingsMenu(userID));
                        execute(editMessageText);

                    }
                    break;
                }
                case "nextEmail":{
                    if (RatesDB.decrementCursor(userID)){
                        EditMessageText editMessageText = new EditMessageText()
                                .setMessageId(callbackQuery.getMessage().getMessageId())
                                .setText(callbackQuery.getMessage().getText())
                                .setChatId(callbackQuery.getMessage().getChatId())
                                .setReplyMarkup(getSettingsMenu(userID));
                        execute(editMessageText);

                    }
                    break;
                }

                case "removeCurrentEmail":{

                    if (RatesDB.removeUserEmail(userID)){
                        EditMessageText editMessageText = new EditMessageText()
                                .setMessageId(callbackQuery.getMessage().getMessageId())
                                .setText(callbackQuery.getMessage().getText())
                                .setChatId(callbackQuery.getMessage().getChatId())
                                .setReplyMarkup(getSettingsMenu(userID));
                        execute(editMessageText);
                    }

                    break;
                }

                case  "sendToNewEmail": {
                    System.out.println("Send new emeal");

                    //AccountManager.setLastCallbackMessageID(userID, callbackQuery.getMessage().getMessageId());
                    sendMessage.setText(LocalizationService.getString("1_SendEmail", userID));
                    execute(sendMessage);

                    break;
                }
                case "backToSettings":
                case "settings":{
                    //AccountManager.setLastCallbackMessageID(userID, callbackQuery.getMessage().getMessageId());
                    EditMessageText editMessageText = new EditMessageText()
                            .setMessageId(callbackQuery.getMessage().getMessageId())
                            .setText(callbackQuery.getMessage().getText())
                            .setChatId(callbackQuery.getMessage().getChatId())
                            .setReplyMarkup(getSettingsMenu(userID));
                    execute(editMessageText);
                    break;
                }
                case "backToMenu":{
                    //AccountManager.setLastCallbackMessageID(userID, callbackQuery.getMessage().getMessageId());
                    EditMessageText editMessageText = new EditMessageText()
                            .setMessageId(callbackQuery.getMessage().getMessageId())
                            .setText(callbackQuery.getMessage().getText())
                            .setChatId(callbackQuery.getMessage().getChatId())
                            .setReplyMarkup(getRatesMessageMenu(userID));
                    execute(editMessageText);
                    break;
                }

                case "addCCY":{
                   // AccountManager.setLastCallbackMessageID(userID, callbackQuery.getMessage().getMessageId());
                    EditMessageText editMessageText = new EditMessageText()
                            .setMessageId(callbackQuery.getMessage().getMessageId())
                            .setText(callbackQuery.getMessage().getText())
                            .setChatId(callbackQuery.getMessage().getChatId())
                            .setReplyMarkup(getRatesAddMenu(userID));
                    execute(editMessageText);

                    break;
                }

                case "removeCCY":{
                    //AccountManager.setLastCallbackMessageID(userID, callbackQuery.getMessage().getMessageId());
                    EditMessageText editMessageText = new EditMessageText()
                            .setMessageId(callbackQuery.getMessage().getMessageId())
                            .setText(callbackQuery.getMessage().getText())
                            .setChatId(callbackQuery.getMessage().getChatId())
                            .setReplyMarkup(getRatesRemoveMenu(userID));
                    execute(editMessageText);

                    break;
                }
                case "getXls":{
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    String rateDateInMessage = callbackQuery.getMessage().getText();
                    rateDateInMessage = rateDateInMessage
                            .substring(0, rateDateInMessage.indexOf('\n'))
                            .replace(LocalizationService.getString("1_RatesOn", userID), "")
                            .replace(" ", "")
                            .replace(" ", "");

                    try {
                        Date DateInMessage =  simpleDateFormat.parse(rateDateInMessage);
                        File xls = RatesService.getXls(DateInMessage);
                        SendDocument sendDocument = new SendDocument()
                                .setChatId(chatID)
                                .setNewDocument(xls);
                        sendDocument(sendDocument);


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case "sendToCurrentEmail":{
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    String rateDateInMessage = callbackQuery.getMessage().getText();
                    rateDateInMessage = rateDateInMessage
                            .substring(0, rateDateInMessage.indexOf('\n'))
                            .replace(LocalizationService.getString("1_RatesOn", userID), "")
                            .replace(" ", "")
                            .replace(" ", "");

                    try {
                        Date DateInMessage =  simpleDateFormat.parse(rateDateInMessage);
                        File xls = RatesService.getXls(DateInMessage);
                        RatesService.sendFile(xls, RatesDB.getCurrentEmail(userID));


                        EditMessageText editMessageText = new EditMessageText()
                                .setMessageId(callbackQuery.getMessage().getMessageId())
                                .setText(callbackQuery.getMessage().getText())
                                .setChatId(callbackQuery.getMessage().getChatId())
                                .setReplyMarkup(getRatesMessageMenu(userID));
                        execute(editMessageText);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                default:{

                    if (callbackData.contains("+CCY")){
                        RatesDB.addUserCurrency(userID, callbackData.substring(4, 7));
                        reloadRatesMessage(callbackQuery);
                    } else if (callbackData.contains("-CCY")){
                        RatesDB.removeUserCurrency(userID, callbackData.substring(4, 7));
                        reloadRatesMessage(callbackQuery);
                    }


                }
            }
        }

    }



    @Override
    public ReplyKeyboardMarkup getModuleMenu(int userID) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        LinkedList<KeyboardRow> rows = new LinkedList<>();
        for(String command : moduleCommands){
            KeyboardRow row = new KeyboardRow();
            row.add(LocalizationService.getString(command, userID));
            rows.add(row);
        }
        KeyboardRow mainRow = new KeyboardRow();
        mainRow.add(LocalizationService.getString("simpleCommandMainMenu", userID));
        rows.add(mainRow);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;
    }
    public InlineKeyboardMarkup getRatesMessageMenu(int userID){

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new LinkedList<>();
        int userEmailsCount = RatesDB.getEmailsCount(userID);

        if(userEmailsCount == 0 ){
            List<InlineKeyboardButton> sendToNewEmailRow = new LinkedList<>();
            sendToNewEmailRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_SendTo", userID))
                    .setCallbackData("sendToNewEmail"));
            rows.add(sendToNewEmailRow);
        } else if (userEmailsCount >0 ){
            List<InlineKeyboardButton> sendToOneEmailRow = new LinkedList<>();
            String userEmail = RatesDB.getCurrentEmail(userID);
            if (userEmail.length()>WIDTH_INLINE_BUTTON){
                userEmail = userEmail.substring(0,WIDTH_INLINE_BUTTON)+"...";
            }
            sendToOneEmailRow.add(new InlineKeyboardButton()
                    .setText(userEmail)
                    .setCallbackData("sendToCurrentEmail"));
            rows.add(sendToOneEmailRow);

        }


        List<InlineKeyboardButton> secondRow = new LinkedList<>();
        secondRow.add(new InlineKeyboardButton()
                .setText(LocalizationService.getString("1_getXls", userID))
                .setCallbackData("getXls"));

        secondRow.add(new InlineKeyboardButton()
                .setText(LocalizationService.getString("1_settings", userID))
                .setCallbackData("settings"));

        rows.add(secondRow);



        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;


      /*    //region First menu
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new LinkedList<>();

        List<InlineKeyboardButton> firsRow = new LinkedList<>();
        firsRow.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_cahngeCCYList", userID)).setCallbackData("cahngeCCYList"));
        rows.add(firsRow);

        int userEmailsCount = RatesDB.getEmailsCount(userID);

        if (userEmailsCount == 0){
            List<InlineKeyboardButton> sendToNewEmailRow = new LinkedList<>();
            sendToNewEmailRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_SendTo", userID))
                    .setCallbackData("sendToNewEmail"));
            rows.add(sendToNewEmailRow);
        } else if (userEmailsCount == 1){
            List<InlineKeyboardButton> sendToOneEmailRow = new LinkedList<>();
            String userEmail = RatesDB.getCurrentEmail(userID);
            if (userEmail.length()>WIDTH_INLINE_BUTTON){
                userEmail = userEmail.substring(0,WIDTH_INLINE_BUTTON)+"...";
            }
            sendToOneEmailRow.add(new InlineKeyboardButton()
                    .setText(userEmail)
                    .setCallbackData("sendToCurrentEmail"));
            rows.add(sendToOneEmailRow);

            List<InlineKeyboardButton> selectEmailsRow = new LinkedList<>();
            selectEmailsRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_removeCurrentEmail", userID))
                    .setCallbackData("removeCurrentEmail"));
            selectEmailsRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_addEmail", userID))
                    .setCallbackData("sendToNewEmail"));
            rows.add(selectEmailsRow);


        } else if (userEmailsCount>1){
            List<InlineKeyboardButton> variableEmailsRow = new LinkedList<>();
            String userEmail = RatesDB.getCurrentEmail(userID);
            if (userEmail.length()>WIDTH_INLINE_BUTTON){
                userEmail = userEmail.substring(0, WIDTH_INLINE_BUTTON)+"...";
            }
            variableEmailsRow.add(new InlineKeyboardButton()
                    .setText(userEmail)
                    .setCallbackData("sendToCurrentEmail"));
            rows.add(variableEmailsRow);

            int nextGap = RatesDB.getEmailCursor(userID);
            int previousGap = RatesDB.getEmailsCount(userID)- RatesDB.getEmailCursor(userID)-1;

            List<InlineKeyboardButton> selectEmailsRow = new LinkedList<>();

            selectEmailsRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_previousEmail", userID)
                            + previousGap)
                    .setCallbackData("previousEmail"));


            selectEmailsRow.add(new InlineKeyboardButton()
                    .setText(nextGap
                            + LocalizationService.getString("1_nextEmail", userID))
                    .setCallbackData("nextEmail"));

            rows.add(selectEmailsRow);


            selectEmailsRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_removeCurrentEmail", userID))
                    .setCallbackData("removeCurrentEmail"));

            selectEmailsRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_addEmail", userID))
                    .setCallbackData("sendToNewEmail"));


        }


        List<InlineKeyboardButton> getXlsRow = new LinkedList<>();
        getXlsRow.add(new InlineKeyboardButton()
                .setText(LocalizationService.getString("1_getXls", userID))
                .setCallbackData("getXls"));
        rows.add(getXlsRow);



        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;

        //endregion  */
    }
    public InlineKeyboardMarkup getSettingsMenu(int userID){
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new LinkedList<>();

        // Кнопки Добавить и удалить валюту
        List<InlineKeyboardButton> firsRow = new LinkedList<>();
        firsRow.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_addCCY", userID)).setCallbackData("addCCY"));
        firsRow.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_removeCCY", userID)).setCallbackData("removeCCY"));
        rows.add(firsRow);


        int userEmailCount = RatesDB.getEmailsCount(userID);

        if (userEmailCount > 1){
            List<InlineKeyboardButton> variableEmailsRow = new LinkedList<>();
            String userEmail = RatesDB.getCurrentEmail(userID);
            if (userEmail.length()>WIDTH_INLINE_BUTTON){
                userEmail = userEmail.substring(0, WIDTH_INLINE_BUTTON)+"...";
        }
            variableEmailsRow.add(new InlineKeyboardButton()
                    .setText(userEmail)
                    .setCallbackData("sendToCurrentEmail"));
            rows.add(variableEmailsRow);

            int nextGap = RatesDB.getEmailCursor(userID);
            int previousGap = RatesDB.getEmailsCount(userID)- RatesDB.getEmailCursor(userID)-1;

            List<InlineKeyboardButton> selectEmailsRow = new LinkedList<>();

            selectEmailsRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_previousEmail", userID)
                            + previousGap)
                    .setCallbackData("previousEmail"));


            selectEmailsRow.add(new InlineKeyboardButton()
                    .setText(nextGap
                            + LocalizationService.getString("1_nextEmail", userID))
                    .setCallbackData("nextEmail"));

            rows.add(selectEmailsRow);


            selectEmailsRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_removeCurrentEmail", userID))
                    .setCallbackData("removeCurrentEmail"));

            selectEmailsRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_addEmail", userID))
                    .setCallbackData("sendToNewEmail"));
        } else if (userEmailCount == 1){
            List<InlineKeyboardButton> sendToOneEmailRow = new LinkedList<>();
            String userEmail = RatesDB.getCurrentEmail(userID);
            if (userEmail.length()>WIDTH_INLINE_BUTTON){
                userEmail = userEmail.substring(0,WIDTH_INLINE_BUTTON)+"...";
            }
            sendToOneEmailRow.add(new InlineKeyboardButton()
                    .setText(userEmail)
                    .setCallbackData("sendToCurrentEmail"));
            rows.add(sendToOneEmailRow);

            List<InlineKeyboardButton> selectEmailsRow = new LinkedList<>();
            selectEmailsRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_removeCurrentEmail", userID))
                    .setCallbackData("removeCurrentEmail"));
            selectEmailsRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_addEmail", userID))
                    .setCallbackData("sendToNewEmail"));
            rows.add(selectEmailsRow);
        }  else {
            List<InlineKeyboardButton> sendToNewEmailRow = new LinkedList<>();
            sendToNewEmailRow.add(new InlineKeyboardButton()
                    .setText(LocalizationService.getString("1_addEmail", userID))
                    .setCallbackData("sendToNewEmail"));
            rows.add(sendToNewEmailRow);
        }




        // Кнопка назад
        List<InlineKeyboardButton> backRow = new LinkedList<>();
        backRow.add(new InlineKeyboardButton()
                .setText(LocalizationService.getString("1_back", userID))
                .setCallbackData("backToMenu"));
        rows.add(backRow);


        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getRatesAddMenu(int userID){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        ArrayList<String> userCurrencies = RatesDB.getUserCurrencies(userID);
        List<List<InlineKeyboardButton>> rows = new LinkedList<>();
        Map<Integer, String> allCurrency = RatesService.currencyPriority;

        List<String> newCurrencies = new LinkedList<>();

        for (Integer key : allCurrency.keySet()){

            if (!userCurrencies.contains(allCurrency.get(key))){
                newCurrencies.add(allCurrency.get(key));
            }

        }

        double columns = 4;
        double newCurrenciesSize = newCurrencies.size()+1;
        int rowsInMenu  = (int) Math.ceil(newCurrenciesSize/columns);
        int counter = 0;
        for (int i = 0; i < rowsInMenu; i++) {
            List<InlineKeyboardButton> row = new LinkedList<>();

            for (int j = 0; j < columns; j++) {
                if (counter>=newCurrencies.size()) {
                    row.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_back", userID)).setCallbackData("backToSettings"));
                    break;
                }
                row.add(new InlineKeyboardButton()
                        .setText(newCurrencies.get(counter))
                        .setCallbackData("+CCY"+newCurrencies.get(counter++)));
            }

            rows.add(row);

        }


        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
    public InlineKeyboardMarkup getRatesRemoveMenu(int userID){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        ArrayList<String> userCurrencies = RatesDB.getUserCurrencies(userID);
        List<List<InlineKeyboardButton>> rows = new LinkedList<>();

        double columns = 3;
        double userCurrenciesSize = userCurrencies.size()+1;
        int rowsInMenu  = (int) Math.ceil(userCurrenciesSize/columns);
        int counter = 0;
        for (int i = 0; i < rowsInMenu; i++) {
            List<InlineKeyboardButton> row = new LinkedList<>();

            for (int j = 0; j < columns; j++) {
                if (counter>=userCurrencies.size()) {
                    row.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_back", userID)).setCallbackData("backToSettings"));
                    break;
                }
                row.add(new InlineKeyboardButton()
                        .setText(userCurrencies.get(counter))
                        .setCallbackData("-CCY"+userCurrencies.get(counter++)));
            }

            rows.add(row);

        }


        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }


    private  void reloadRatesMessage(CallbackQuery callbackQuery){
        int userID = callbackQuery.getFrom().getId();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String rateDateInMessage = callbackQuery.getMessage().getText();
        rateDateInMessage = rateDateInMessage
                .substring(0, rateDateInMessage.indexOf('\n'))
                .replace(LocalizationService.getString("1_RatesOn", userID), "")
                .replace(" ", "")
                .replace(" ", "");


        EditMessageText editMessageText = null;
        try {
            editMessageText = new EditMessageText()
                    .setMessageId(callbackQuery.getMessage().getMessageId())
                    .setText(RatesService.getRatesMessage(simpleDateFormat.parse(rateDateInMessage), userID))
                    .setChatId(callbackQuery.getMessage().getChatId())
                    .setReplyMarkup(getRatesMessageMenu(userID));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



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
}
