package modules;

import com.vdurmont.emoji.EmojiParser;
import dao.RatesLoader;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RatesModule implements BotModule {


    @Override
    public Boolean acceptUpdate(String command) {
        if (command.equals(this.getAction())) { return true;}
        return false;
    }//

    @Override
    public SendMessage getResponce(Update getUpdate) {
        SendMessage sendMessage = new SendMessage();
        if (getUpdate.hasMessage()){
            sendMessage.setChatId(getUpdate.getMessage().getChatId());
            String message = getUpdate.getMessage().getText();

            if (message.equals(this.getAction())){
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                List<KeyboardRow> rows = new LinkedList<>();
                KeyboardRow row1 = new KeyboardRow();
                row1.add("Актуальные");
                rows.add(row1);
                KeyboardRow row2 = new KeyboardRow();
                row2.add("В главное меню");
                rows.add(row2);
                replyKeyboardMarkup.setKeyboard(rows);
                replyKeyboardMarkup.setResizeKeyboard(true);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                sendMessage.setText("Нажмите \"Актуальные\" или пришлите дату в чат в формате \"ДД.ММ.ГГГГ\"");
            } else  if (message.equals("Актуальные")) {
                try {
                    RatesLoader ratesLoader = new RatesLoader();
                    sendMessage.setText(ratesLoader.getCurrentRates());
                } catch (IOException e) {
                    System.out.println("Catch on 57 line in RatesModule");
                    e.printStackTrace();
                }
            } else {
                SimpleDateFormat myDateFormat = new SimpleDateFormat("dd.MM.yyyy");
                Date searchDate;
                try {
                    message = message.replace(',','.');
                    searchDate = myDateFormat.parse(message);
                    try {
                        RatesLoader ratesLoader = new RatesLoader();
                        String answer = ratesLoader.getRatesOnDate(searchDate);
                        sendMessage.setText(answer);
                    } catch (IOException e) {
                        sendMessage.setText("Курсы не найдены. Повторите запрос ");
                    }

                } catch (ParseException e) {
                    sendMessage.setText("Не удается распознать дату. Попробуйте снова");
                }
            }


        }else if (getUpdate.hasCallbackQuery()){
            sendMessage.setChatId(getUpdate.getCallbackQuery().getMessage().getChatId());
            sendMessage.setText("Работаем с курсами (Callback)");
        }
        return sendMessage;
    }

    @Override
    public String getAction() {
        return "Курсы валют";
    }





}
