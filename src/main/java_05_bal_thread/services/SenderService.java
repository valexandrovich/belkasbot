package services;

import database.AccountManager;
import modules.IModule;
import modules.language.LanguageModule;
import modules.rates.RatesModule;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;

import java.util.LinkedList;
import java.util.concurrent.Callable;

public class SenderService implements Callable<LinkedList<BotApiMethod>>{

    private Update update;
    private final static String LOGTAG = "SenderService";

    long callBackMessageID;
    long chatID;
    int userID;
    String userName;
    String moduleTitle;
    String messageText;


    private static LinkedList<IModule> modulesList = new LinkedList<>();

    static {
        modulesList.add(new RatesModule());
        modulesList.add(new LanguageModule());
    }

    public static LinkedList<IModule> getModulesList() {
        return modulesList;
    }

    public SenderService(Update update) {
        this.update = update;
    }

    @Override
    public LinkedList<BotApiMethod> call() throws Exception {
        if (update.hasMessage()){
            userID = update.getMessage().getFrom().getId();
            chatID = update.getMessage().getChatId();
            messageText = update.getMessage().getText();
        } else if (update.hasCallbackQuery()){
            userID = update.getCallbackQuery().getFrom().getId();
            chatID = update.getCallbackQuery().getMessage().getChatId();
            messageText = update.getCallbackQuery().getData();
        }

        if (SimpleCommandService.isSimpleCommand(messageText, userID)){
            return SimpleCommandService.handleSimpleCommand(messageText, chatID, userID);
        } else {
            checkModuleEntering(messageText, userID);
            int userState = AccountManager.getUserState(userID);
            for (IModule module : modulesList){
                if (module.getModuleCode() == userState){
                    try {
                        return module.handle(update);
                    } catch (Throwable throwable) {
                        // Catch Exception from module
                        throwable.printStackTrace();
                    }
                }
            }

            // Sending Main menu if module cant' find
            LinkedList<BotApiMethod> mainMenuList = new LinkedList<>();
            mainMenuList.add(SimpleCommandService.getMainMenu(userID, chatID));
            return mainMenuList;

        }




    }

    private void checkModuleEntering(String command, int userID){
        for (IModule module : modulesList){
            if (module.getModuleTitle(userID).equals(command)){
                AccountManager.setUserState(userID, module.getModuleCode());
            }
        }
    }


}
