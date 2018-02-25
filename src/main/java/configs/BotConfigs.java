package configs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class BotConfigs {
    private static String BOT_NAME;
    private static String BOT_TOKEN;

    static {
        String envBotToken = System.getenv("BOT_TOKEN");
        String envBotName = System.getenv("BOT_NAME");

        if (envBotName == null || envBotToken == null){
            Properties botProperties = new Properties();
            try {
                botProperties.load(new FileInputStream(new File("configs/bot.properties")));
                BOT_TOKEN = botProperties.getProperty("BOT_TOKEN");
                BOT_NAME = botProperties.getProperty("BOT_NAME");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            BOT_NAME = envBotName;
            BOT_TOKEN = envBotToken;
        }
    }

    public static String getBotName() {
        return BOT_NAME;
    }

    public static String getBotToken() {
        return BOT_TOKEN;
    }
}
