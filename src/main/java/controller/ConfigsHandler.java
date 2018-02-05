package controller;

import java.io.*;
import java.util.Properties;

public class ConfigsHandler {

    public static final String BOT_PROPERTIES_PATH = "./config/bot.properties";

    public static void loadBotConfigs(Properties botProperties) {
        try (InputStream in = new FileInputStream(new File(BOT_PROPERTIES_PATH))) {
            botProperties.load(in);
        } catch (Exception ex) {
            System.out.println("Error configs load");
        }
    }

}
