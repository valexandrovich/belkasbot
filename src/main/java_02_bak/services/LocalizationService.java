package services;

import database.AccountManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * @author Ruben Bermudez
 * @version 1.0
 */
public class LocalizationService {
    private static final String STRINGS_FILE = "strings";
    private static final Object lock = new Object();

    private static final List<Language> supportedLanguages = new ArrayList<>();
    private static final Utf8ResourceBundle english;
    private static final Utf8ResourceBundle russian;
    private static final Utf8ResourceBundle ukrainian;
    private static final Utf8ResourceBundle emoji;


    static {
        synchronized (lock) {
            english = new Utf8ResourceBundle(STRINGS_FILE, new Locale("en", "US"));
            supportedLanguages.add(new Language("en", "English", "\uD83C\uDDFA\uD83C\uDDF8"));
            russian = new Utf8ResourceBundle(STRINGS_FILE, new Locale("ru", "RU"));
            supportedLanguages.add(new Language("ru", "Русский", "\uD83C\uDDF7\uD83C\uDDFA"));
            ukrainian = new Utf8ResourceBundle(STRINGS_FILE, new Locale("uk", "UA"));
            supportedLanguages.add(new Language("uk", "Українська", "\uD83C\uDDFA\uD83C\uDDE6"));
            emoji = new Utf8ResourceBundle(STRINGS_FILE, new Locale("emoji", ""));



        }
    }

    /**
     * Get a string in default language (en)
     *
     * @param key key of the resource to fetch
     * @return fetched string or error message otherwise
     */
    private static String getString(String key) {
        String result;
        try {
            result = english.getString(key);
        } catch (MissingResourceException e) {
            result = "String not found";
        }

        return result;
    }


    public static String getString(String key, int userID){
        String result;
        String language = AccountManager.getUserLanguage(userID);
        try {
            switch (language.toLowerCase()) {
                case "en":
                case "en-us":
                    result = english.getString(key);
                    break;
                case "ru":
                case "ru-ru":
                    result = russian.getString(key);
                    break;
                case "uk":
                case "uk-ua":
                    result = ukrainian.getString(key);
                    break;
                default: result = english.getString(key);

            }
        } catch (MissingResourceException e) {
            result = english.getString(key);
        }

       try{
           return emoji.getString(key) + " " +  result;
       } catch (MissingResourceException e){
           return result;
       }

    }

    public static List<Language> getSupportedLanguages() {
        return supportedLanguages;
    }

    public static Language getLanguageByCode(String languageCode) {
        return supportedLanguages.stream().filter(x -> x.getCode().equals(languageCode)).findFirst().orElse(null);
    }

    public static Language getLanguageByName(String languageName) {
        return supportedLanguages.stream().filter(x -> x.getName().equals(languageName)).findFirst().orElse(null);
    }

    public static String getLanguageEmoji(int userID){
        String emoji = "";
        String languageCode = AccountManager.getUserLanguage(userID);
        emoji = getLanguageByCode(languageCode).getEmoji();
        return emoji;
    }

    public static String getLanguageCodeByName(String language) {
        return supportedLanguages.stream().filter(x -> x.getName().equals(language))
                .map(Language::getCode).findFirst().orElse(null);
    }

    public static class Language {
        private String code;
        private String name;
        private String emoji;

        public Language(String code, String name, String emoji) {
            this.code = code;
            this.name = name;
            this.emoji = emoji;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmoji() {
            return emoji;
        }

        public void setEmoji(String emoji) {
            this.emoji = emoji;
        }

        @Override
        public String toString() {
            if (emoji == null || emoji.isEmpty()) {
                return name;
            } else {
                return emoji + " " + name;
            }
        }
    }

    private static class Utf8ResourceBundle extends ResourceBundle {

        private static final String BUNDLE_EXTENSION = "properties";
        private static final String CHARSET = "UTF-8";
        private static final Control UTF8_CONTROL = new UTF8Control();

        Utf8ResourceBundle(String bundleName, Locale locale) {
            setParent(ResourceBundle.getBundle(bundleName, locale, UTF8_CONTROL));
        }

        @Override
        protected Object handleGetObject(String key) {
            return parent.getObject(key);
        }

        @Override
        public Enumeration<String> getKeys() {
            return parent.getKeys();
        }

        private static class UTF8Control extends Control {
            public ResourceBundle newBundle
                    (String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                    throws IllegalAccessException, InstantiationException, IOException {
                String bundleName = toBundleName(baseName, locale);
                String resourceName = toResourceName(bundleName, BUNDLE_EXTENSION);
                ResourceBundle bundle = null;
                InputStream stream = null;
                if (reload) {
                    URL url = loader.getResource(resourceName);
                    if (url != null) {
                        URLConnection connection = url.openConnection();
                        if (connection != null) {
                            connection.setUseCaches(false);
                            stream = connection.getInputStream();
                        }
                    }
                } else {
                    stream = loader.getResourceAsStream(resourceName);
                }
                if (stream != null) {
                    try {
                        bundle = new PropertyResourceBundle(new InputStreamReader(stream, CHARSET));
                    } finally {
                        stream.close();
                    }
                }
                return bundle;
            }
        }
    }
}