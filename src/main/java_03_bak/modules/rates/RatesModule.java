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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RatesModule extends TelegramLongPollingBot implements IModule {

    private static SimpleDateFormat shortDate = new SimpleDateFormat("dd.MM.yyyy");
    private final String LOGTAG = "RatesModule";


    @Override
    public String getModuleTitle(int userID) {
        return LocalizationService.getString(getModuleCode()+"_Title", userID);
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
        }
    }

    @Override
    public void handleMessage(Message message) throws TelegramApiException, IOException {
        int userID = message.getFrom().getId();
        long chatID = message.getChatId();

        SendMessage sendMessage = new SendMessage()
                .setChatId(chatID);

        try {

            // Check module entering
            if (message.getText().equals(LocalizationService.getString(getModuleCode() + "_Title", message.getFrom().getId()))) {
                sendMessage.setText(LocalizationService.getString(getModuleCode() + "_Welcome", userID))
                        .setReplyMarkup(getModuleMenu(userID));
                execute(sendMessage);
            } else {
                if (message.getText().equals(LocalizationService.getString(getModuleCode() + "_RatesActual", userID))) {

                    // Actual Rates
                    sendMessage.setText(RatesService.getRatesMessage(new Date(System.currentTimeMillis()), userID))
                            .setReplyMarkup(getRatesMessageMenu(userID));
                    execute(sendMessage);
                } else if (message.getText().equals(LocalizationService.getString(getModuleCode() + "_RatesMonth", userID))) {

                    // Month Rates
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    sendMessage.setText(RatesService.getRatesMessage(new Date(calendar.getTimeInMillis()), userID))
                            .setReplyMarkup(getRatesMessageMenu(userID));
                    execute(sendMessage);
                } else if (message.getText().contains("@") && message.getText().contains(".")) {

                    // Add new email
                    if (RatesDB.addUserEmail(userID, message.getText())) {
                        sendMessage.setText(LocalizationService.getString("1_EmailAdded", userID) + " " + message.getText());
                        execute(sendMessage);
                        RatesDB.setEmailCursor(userID, 0);
                        sendMessage.setText(RatesService.getRatesMessage(RatesDB.getLastRateDate(userID), userID))
                                .setReplyMarkup(getRatesMessageMenu(userID));
                        execute(sendMessage);
                    }
                    return;
                } else {

                    // Trying parse date
                    sendMessage.setText(RatesService.getRatesMessage(shortDate
                            .parse(message.getText()
                                    .replace(",", ".")
                                    .trim()
                                    .replace(" ", "")), userID))
                                    .setReplyMarkup(getRatesMessageMenu(userID));
                    execute(sendMessage);
                }
            }

        } catch (RatesException e) {
            LoggerService.logError(LOGTAG, e);
            sendMessage.setText(e.getMessage());
            execute(sendMessage);
        } catch (ParseException e) {
            LoggerService.logError(LOGTAG, new Throwable(LocalizationService.getString("1_RatesParseException", userID)
                    + " \"" + message.getText() + "\""));
            sendMessage.setText(LocalizationService.getString("1_RatesParseException", userID)
                    + " \"" + message.getText() + "\"" );
            execute(sendMessage);
        }

    }
    @Override
    public void handleCallbackQuery(CallbackQuery callbackQuery) throws Throwable {
        int userID = callbackQuery.getFrom().getId();
        long chatID = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();

        SendMessage sendMessage = new SendMessage()
                .setChatId(chatID);

        EditMessageText editMessageText = new EditMessageText()
                .setChatId(chatID)
                .setMessageId(callbackQuery.getMessage().getMessageId())
                .setText(callbackQuery.getMessage().getText());


        switch (callbackData){
            case "sendToNewEmail":{
                sendMessage.setText(LocalizationService.getString("1_NewEmail", userID));
                execute(sendMessage);
                return;
            }
            case "sendToCurrentEmail":{
                RatesService.sendFile(getRateDateFromCallbackMessage(callbackQuery), RatesDB.getCurrentUserEmail(userID));
                sendMessage.setText(LocalizationService.getString("1_Sent", userID));
                execute(sendMessage);
                reloadLastRatesMessage(callbackQuery);
                return;
            }
            case "downloadXls":{
                SendDocument sendDocument = new SendDocument()
                        .setNewDocument(RatesService.getRatesXlsFile(getRateDateFromCallbackMessage(callbackQuery)))
                        .setChatId(chatID);
                sendDocument(sendDocument);
                return;
            }
            case "settings":{
                editMessageText.setReplyMarkup(getRatesMessageSettingsMenu(userID));
                execute(editMessageText);
                return;

            }

            case "ratesMenu":{
                editMessageText.setReplyMarkup(getRatesMessageMenu(userID));
                execute(editMessageText);
                return;
            }

            case "previousEmail":{
                if (RatesDB.incrementEmailCursor(userID)){
                    reloadLastRatesMessageSettings(callbackQuery);
                }
                return;
            }
            case "nextEmail":{
                if (RatesDB.decrementEmailCursor(userID)){
                    reloadLastRatesMessageSettings(callbackQuery);
                }
                return;
            }
            case "removeCurrentEmail": {
                if (RatesDB.removeUserEmail(userID, RatesDB.getCurrentUserEmail(userID))) {
                    reloadLastRatesMessageSettings(callbackQuery);
                }
                return;
            }
            case "addCCYmenu":{
                reloadLastRatesMessageAddCCY(callbackQuery);
                return;
            }
            case "removeCCYmenu":{
                reloadLastRatesMessageRemoveCCY(callbackQuery);
                return;
            }
            default:{
                if (callbackData.contains("+CCY")){
                    if (RatesDB.addUserCurrency(userID, callbackData.replace("+CCY", ""))){
                        reloadLastRatesMessageAddCCY(callbackQuery);
                    }
                } else if (callbackData.contains("-CCY")){
                    if (RatesDB.removeUserCurrency(userID, callbackData.replace("-CCY", ""))){
                        reloadLastRatesMessageRemoveCCY(callbackQuery);
                    }
                }

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

        KeyboardRow rowActualRates = new KeyboardRow();
        rowActualRates.add(LocalizationService.getString(getModuleCode()+"_RatesActual", userID));
        rows.add(rowActualRates);

        KeyboardRow rowMonthRates = new KeyboardRow();
        rowMonthRates.add(LocalizationService.getString(getModuleCode()+"_RatesMonth", userID));
        rows.add(rowMonthRates);

        KeyboardRow rowMainMenuRow = new KeyboardRow();
        rowMainMenuRow.add(LocalizationService.getString("SimpleCommandMainMenu", userID));
        rows.add(rowMainMenuRow);

        replyKeyboardMarkup.setKeyboard(rows);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;

    }
    private InlineKeyboardMarkup getRatesMessageMenu(int userID){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new LinkedList<>();

        int userEmailCount = RatesDB.getEmailsCount(userID);

        // email row
        if (userEmailCount == 0){
            List<InlineKeyboardButton> emailRow = new LinkedList<>();
            emailRow.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_SendTo", userID)).setCallbackData("sendToNewEmail"));
            rows.add(emailRow);
        } else {
            List<InlineKeyboardButton> emailRow = new LinkedList<>();
            emailRow.add(new InlineKeyboardButton().setText(RatesDB.getCurrentUserEmail(userID)).setCallbackData("sendToCurrentEmail"));
            rows.add(emailRow);
        }

        // Settings and Download row
        List<InlineKeyboardButton> secondRow = new LinkedList<>();
        secondRow.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_DownloadXLS", userID)).setCallbackData("downloadXls"));
        secondRow.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_Settings", userID)).setCallbackData("settings"));
        rows.add(secondRow);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
    private InlineKeyboardMarkup getRatesMessageSettingsMenu(int userID){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new LinkedList<>();

        // Add and Remove CCY row
        List<InlineKeyboardButton> ccyChangeRow = new LinkedList<>();
        ccyChangeRow.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_RemoveCCY", userID)).setCallbackData("removeCCYmenu"));
        ccyChangeRow.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_AddCCY", userID)).setCallbackData("addCCYmenu"));
        rows.add(ccyChangeRow);

        int userEmailsCount = RatesDB.getEmailsCount(userID);
        int currentEmailCursor = RatesDB.getEmailCursor(userID);

        // Current email row
        List<InlineKeyboardButton> currentEmailRow = new LinkedList<>();
        if (userEmailsCount == 0){
            currentEmailRow.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_SendTo", userID)).setCallbackData("sendToNewEmail"));
        } else {
            currentEmailRow.add(new InlineKeyboardButton().setText(RatesDB.getCurrentUserEmail(userID)).setCallbackData("sendToCurrentEmail"));
        }
        rows.add(currentEmailRow);

        // Manage email buttons menu
        if (userEmailsCount > 0){
            List<InlineKeyboardButton> manageEmails = new LinkedList<>();
            if (userEmailsCount == 1){
                // without forward\back buttons
                manageEmails.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_RemoveCurrentEmail", userID)).setCallbackData("removeCurrentEmail"));
                manageEmails.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_AddNewEmail", userID)).setCallbackData("sendToNewEmail"));
            } else {
                int gapEmailsBack = userEmailsCount - currentEmailCursor - 1;
                int gapEmailsForward = currentEmailCursor;
                manageEmails.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_PreviousEmail", userID) + gapEmailsBack).setCallbackData("previousEmail"));
                manageEmails.add(new InlineKeyboardButton().setText(gapEmailsForward + LocalizationService.getString("1_NextEmail", userID)).setCallbackData("nextEmail"));
                manageEmails.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_AddNewEmail", userID)).setCallbackData("sendToNewEmail"));
                manageEmails.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_RemoveCurrentEmail", userID)).setCallbackData("removeCurrentEmail"));
            }
            rows.add(manageEmails);
        }

        // BackRow
        List<InlineKeyboardButton> backRow = new LinkedList<>();
        backRow.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_BackToMenu", userID)).setCallbackData("ratesMenu"));
        rows.add(backRow);

        inlineKeyboardMarkup.setKeyboard(rows);
        return  inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getAddCCYMenu(int userID){
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
                    row.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_BackToMenu", userID)).setCallbackData("settings"));
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
    private InlineKeyboardMarkup getRemoveCCYMenu(int userID){
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
                    row.add(new InlineKeyboardButton().setText(LocalizationService.getString("1_BackToMenu", userID)).setCallbackData("settings"));
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

    private void reloadLastRatesMessage(CallbackQuery callbackQuery){
        long chatID = callbackQuery.getMessage().getChatId();
        int userID = callbackQuery.getFrom().getId();
        try {
        String date = callbackQuery.getMessage().getText()
                .substring(0, callbackQuery.getMessage().getText().indexOf("\n"))
                .trim()
                .replace(LocalizationService.getString("1_RatesOn", userID), "")
                .replace(" ", "");
            Date rateDateInMessage = shortDate.parse(date);

        EditMessageText editMessageText = new EditMessageText()
                .setChatId(chatID)
                .setMessageId((int) AccountManager.getLastCallbackMessageID(userID))
                .setText(RatesService.getRatesMessage(rateDateInMessage, userID))
                .setReplyMarkup(getRatesMessageMenu(userID));

        execute(editMessageText);

        } catch (ParseException e) {
            LoggerService.logError(LOGTAG, e);
        } catch (RatesException e) {
            LoggerService.logError(LOGTAG, e);
        } catch (TelegramApiException e) {
            LoggerService.logError(LOGTAG, e);
        }
    }
    private void reloadLastRatesMessageSettings(CallbackQuery callbackQuery){
        long chatID = callbackQuery.getMessage().getChatId();
        int userID = callbackQuery.getFrom().getId();
        try {
        String date = callbackQuery.getMessage().getText()
                .substring(0, callbackQuery.getMessage().getText().indexOf("\n"))
                .trim()
                .replace(LocalizationService.getString("1_RatesOn", userID), "")
                .replace(" ", "");
            Date rateDateInMessage = shortDate.parse(date);

        EditMessageText editMessageText = new EditMessageText()
                .setChatId(chatID)
                .setMessageId((int) AccountManager.getLastCallbackMessageID(userID))
                .setText(RatesService.getRatesMessage(rateDateInMessage, userID))
                .setReplyMarkup(getRatesMessageSettingsMenu(userID));


        execute(editMessageText);

        } catch (ParseException e) {
            LoggerService.logError(LOGTAG, e);
        } catch (RatesException e) {
            LoggerService.logError(LOGTAG, e);
        } catch (TelegramApiException e) {
            LoggerService.logError(LOGTAG, e);
        }
    }
    private void reloadLastRatesMessageAddCCY(CallbackQuery callbackQuery){
        long chatID = callbackQuery.getMessage().getChatId();
        int userID = callbackQuery.getFrom().getId();
        try {
        String date = callbackQuery.getMessage().getText()
                .substring(0, callbackQuery.getMessage().getText().indexOf("\n"))
                .trim()
                .replace(LocalizationService.getString("1_RatesOn", userID), "")
                .replace(" ", "");
            Date rateDateInMessage = shortDate.parse(date);

        EditMessageText editMessageText = new EditMessageText()
                .setChatId(chatID)
                .setMessageId((int) AccountManager.getLastCallbackMessageID(userID))
                .setText(RatesService.getRatesMessage(rateDateInMessage, userID))
                .setReplyMarkup(getAddCCYMenu(userID));


        execute(editMessageText);

        } catch (ParseException e) {
            LoggerService.logError(LOGTAG, e);
        } catch (RatesException e) {
            LoggerService.logError(LOGTAG, e);
        } catch (TelegramApiException e) {
            LoggerService.logError(LOGTAG, e);
        }
    }
    private void reloadLastRatesMessageRemoveCCY(CallbackQuery callbackQuery){
        long chatID = callbackQuery.getMessage().getChatId();
        int userID = callbackQuery.getFrom().getId();
        try {
        String date = callbackQuery.getMessage().getText()
                .substring(0, callbackQuery.getMessage().getText().indexOf("\n"))
                .trim()
                .replace(LocalizationService.getString("1_RatesOn", userID), "")
                .replace(" ", "");
            Date rateDateInMessage = shortDate.parse(date);

        EditMessageText editMessageText = new EditMessageText()
                .setChatId(chatID)
                .setMessageId((int) AccountManager.getLastCallbackMessageID(userID))
                .setText(RatesService.getRatesMessage(rateDateInMessage, userID))
                .setReplyMarkup(getRemoveCCYMenu(userID));


        execute(editMessageText);

        } catch (ParseException e) {
            LoggerService.logError(LOGTAG, e);
        } catch (RatesException e) {
            LoggerService.logError(LOGTAG, e);
        } catch (TelegramApiException e) {
            LoggerService.logError(LOGTAG, e);
        }
    }

    private Date getRateDateFromCallbackMessage(CallbackQuery callbackQuery) {
        long chatID = callbackQuery.getMessage().getChatId();
        int userID = callbackQuery.getFrom().getId();
        Date rateDateInMessage = new Date(System.currentTimeMillis());
        try {
            String date = callbackQuery.getMessage().getText()
                    .substring(0, callbackQuery.getMessage().getText().indexOf("\n"))
                    .trim()
                    .replace(LocalizationService.getString("1_RatesOn", userID), "")
                    .replace(" ", "");
            rateDateInMessage = shortDate.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rateDateInMessage;
    }


        // region Override Bot Methods

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

    //endregion
}
