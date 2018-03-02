package modules.rates;

import database.AccountManager;
import modules.IModule;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import services.LocalizationService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RatesModule implements IModule {

    private final static String LOGTAG = "RatesModule";
    private static SimpleDateFormat shortDate = new SimpleDateFormat("dd.MM.yyyy");
    long callBackMessageID;
    long chatID;
    int userID;
    String userName;
    String moduleTitle;
    String messageText;
    String callbackData;

    @Override
    public String getModuleTitle(int userID) {
        return LocalizationService.getString(getModuleCode()+"_Title", userID);
    }

    @Override
    public int getModuleCode() {
        return 1;
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
        try{

            if (messageText.equals(getModuleTitle(userID))){
                responseList.add(getModuleMenu());
            } else {
                if (messageText.contains(LocalizationService.getString(getModuleCode()+"_RatesActual", userID))){
                    // Actual Rates
                    SendMessage sendMessage = new SendMessage()
                            .setChatId(chatID)
                            .setText(RatesService.getRatesMessage(new Date(System.currentTimeMillis()), userID))
                            .setReplyMarkup(getRatesMessageMenu(userID));
                    responseList.add(sendMessage);
                    RatesDB.addRatesResponce(userID, new java.sql.Date(System.currentTimeMillis()));
                } else if (messageText.contains(LocalizationService.getString(getModuleCode()+"_RatesMonth", userID))){
                    // Month Rate
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    SendMessage sendMessage = new SendMessage()
                            .setChatId(chatID)
                            .setText(RatesService.getRatesMessage(new Date(calendar.getTimeInMillis()), userID))
                            .setReplyMarkup(getRatesMessageMenu(userID));
                    responseList.add(sendMessage);
                    RatesDB.addRatesResponce(userID, new java.sql.Date(calendar.getTimeInMillis()));
                } else if (messageText.contains("@") && messageText.contains(".")){
                    // Add email
                    if (RatesDB.addUserEmail(userID, messageText)){
                        SendMessage sendMessage = new SendMessage()
                                .setChatId(chatID)
                                .setText(LocalizationService.getString(getModuleCode()+"_EmailAdded", userID));
                        responseList.add(sendMessage);
                        responseList.add(reloadLastRatesMessage(userID));

                    }
                } else {
                    // Trying parse
                    Date date = shortDate.parse(messageText);
                    SendMessage sendMessage = new SendMessage()
                            .setChatId(chatID)
                            .setText(RatesService.getRatesMessage(date, userID))
                            .setReplyMarkup(getRatesMessageMenu(userID));
                    responseList.add(sendMessage);
                    RatesDB.addRatesResponce(userID, date);
                }
            }

        } catch (RatesException re){
            // Can't find rates on date
            SendMessage sendMessage = new SendMessage()
                    .setChatId(chatID)
                    .setText(LocalizationService.getString(getModuleCode()+"_RatesNotExist", userID));
            responseList.clear();
            responseList.add(sendMessage);
            responseList.add(getModuleMenu());

        } catch (ParseException e) {
            // Can't parse Date
            SendMessage sendMessage = new SendMessage()
                    .setChatId(chatID)
                    .setText(LocalizationService.getString(getModuleCode()+"_RatesParseException", userID));
            responseList.clear();
            responseList.add(sendMessage);
            //responseList.add(getModuleMenu());
        }
        return responseList;
    }

    @Override
    public LinkedList<BotApiMethod> handleCallbackQuery(Update update) {
        LinkedList<BotApiMethod> responseList = new LinkedList<>();
        try {
            SendMessage sendMessage = new SendMessage()
                    .setChatId(chatID);
            switch (callbackData) {
                case "sendToNewEmail": {
                    sendMessage.setText(LocalizationService.getString("1_NewEmail", userID));
                    responseList.add(sendMessage);
                    break;
                }
                case "sendToCurrentEmail": {
                    Date lastCallbackDate = getRateDateFromCallbackMessage(update.getCallbackQuery());
                    RatesService.sendFile(lastCallbackDate, RatesDB.getCurrentUserEmail(userID));
                    sendMessage.setText(LocalizationService.getString(getModuleCode()+"_Sent", userID));
                    responseList.add(sendMessage);
                    //responseList.add(reloadLastRatesMessage(userID));
                    break;
                }
                case "downloadXls":{
                    SendDocument sendDocument = new SendDocument()
                            .setNewDocument(RatesService.getRatesXlsFile(getRateDateFromCallbackMessage(callbackQuery)))
                            .setChatId(chatID);
                    responseList.add(sendDocument);

                    return;
                }
            }

        } catch (RatesException e) {
            e.printStackTrace();
        }
        return responseList;
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
            callbackData = update.getCallbackQuery().getData();
        }
    }

    @Override
    public boolean checkModuleEntering(String messageText) {
        return false;
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



    private SendMessage reloadLastRatesMessage(int userID) throws RatesException {
        Date lastDate = RatesDB.getLastRateDate(userID);
        SendMessage sendMessage = new SendMessage()
                .setChatId(chatID);
        sendMessage.setText(RatesService.getRatesMessage(RatesDB.getLastRateDate(userID), userID))
                .setReplyMarkup(getRatesMessageMenu(userID));
        return sendMessage;
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
            System.out.println(date);
            rateDateInMessage = shortDate.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rateDateInMessage;
    }

    @Override
    public SendMessage getModuleMenu() {
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

        SendMessage sendMessage = new SendMessage()
                .setChatId(chatID)
                .setText(LocalizationService.getString(getModuleCode()+"_Welcome", userID))
                .setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;


    }
}
