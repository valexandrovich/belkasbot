package modules;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

import java.util.List;

public interface BotModule {

    public Boolean acceptUpdate(String command);
    public SendMessage getResponce(Update update);
    public String getAction();


}
