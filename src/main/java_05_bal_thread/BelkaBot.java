import com.google.common.util.concurrent.*;
import configs.BotConfigs;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import services.LoggerService;
import services.SenderService;

import java.util.LinkedList;
import java.util.concurrent.Executors;

public class BelkaBot extends TelegramLongPollingBot {

    private ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    private SenderService senderService;
    private static final String LOGTAG = "BelkaBot";


    @Override
    public void onUpdateReceived(Update update) {
        senderService = new SenderService(update);

        ListenableFuture<LinkedList<BotApiMethod>> future = executorService.submit(senderService);
        Futures.addCallback(future, new FutureCallback<LinkedList<BotApiMethod>>() {
            @Override
            public void onSuccess(LinkedList<BotApiMethod> botApiMethods) {
                if (botApiMethods.size()>0){
                    for (BotApiMethod method : botApiMethods){
                        try {
                            execute(method);
                        } catch (TelegramApiException e) {
                            LoggerService.logError(LOGTAG, e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                LoggerService.logError(LOGTAG, throwable);
            }
        });

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
