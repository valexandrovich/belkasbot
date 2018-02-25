package modules;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

public class AccountsModule implements BotModule {
    @Override
    public Boolean acceptUpdate(String command) {
        return null;
    }

    @Override
    public SendMessage getResponce(Update update) {
        return null;
    }

    @Override
    public String getAction() {
        return null;
    }
}
