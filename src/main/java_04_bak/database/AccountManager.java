package database;

import modules.rates.RatesDB;
import org.telegram.telegrambots.api.objects.User;
import services.LoggerService;

import java.sql.*;

public class AccountManager {
    private static final String LOGTAG = "AccountManager";
    private static Object lock = new Object();

    private static volatile Connection connection;

    private AccountManager(){};

    static {
        LoggerService.logInfo(LOGTAG, "Initialize connection");
        connection = ConnectorDB.getConnection();
    }


    public static boolean isSessionExist(int userID) {
        boolean exist = false;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT userID FROM tb_users WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                exist = true;
                LoggerService.logInfo(LOGTAG, "User exist " + getUserName(userID));
            }
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return exist;
    }
    public static boolean createSession(User user, long chatID){
        int updatedRows = 0;
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tb_users VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, user.getId());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setBoolean(4, user.getBot());
            preparedStatement.setString(5, user.getUserName());
            preparedStatement.setString(6,  nvl(user.getLanguageCode(), "ru-ru"));
            preparedStatement.setLong(7, chatID);
            preparedStatement.setInt(8, 0);
            preparedStatement.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setLong(10, 0);
            updatedRows = preparedStatement.executeUpdate();
            LoggerService.logAction(LOGTAG,  getUserName(user.getId())
                    +"Creating new session");

            // additional initialize

            RatesDB.initializeDefaulCurrenciesForUser(user.getId());

        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return updatedRows>0;
    }

    public static String getUserName(int userID){
        String userName = "";
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT userID, firstname, lastname FROM tb_users WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                userName = resultSet.getInt(1) + " "
                        + resultSet.getString(2) + " "
                        + resultSet.getString(3)
                        +" : ";
            }
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return userName;
    }

    public static int getUserState(int userID){
        int userState = 0;

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT state FROM tb_users WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()){
                userState = result.getInt(1);
            }
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return userState;
    }
    public static boolean setUserState(int userID, int state){
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE tb_users SET state = ? WHERE userID = ?");
            preparedStatement.setInt(1, state);
            preparedStatement.setInt(2, userID);
            updatedRows = preparedStatement.executeUpdate();
            if (updatedRows>0) {LoggerService.logAction(LOGTAG, getUserName(userID)
                    + "Set new user state = " + state);}
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return updatedRows>0;
    }

    public static String getUserLanguage(int userID){
        String userLanguage = "ru";

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT languageCode FROM tb_users WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()){
                userLanguage = result.getString(1);
            }
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return userLanguage;
    }
    public static boolean setUserLanguage(int userID, String languageCode){
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE tb_users SET languageCode = ? WHERE userID = ?");
            preparedStatement.setString(1, languageCode);
            preparedStatement.setInt(2, userID);
            updatedRows = preparedStatement.executeUpdate();
            if (updatedRows>0) {LoggerService.logAction(LOGTAG,  getUserName(userID)
                    +"Change user languageCode to " + languageCode);}
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return updatedRows>0;
    }

    public static long getLastCallbackMessageID(int userID){
        long lastCallbackMessageID = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT lastCallbackMessageID FROM tb_users WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                lastCallbackMessageID = resultSet.getLong(1);
            }
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return lastCallbackMessageID;
    }
    public static boolean setLastCallbackMessageID(int userID, long messageID){
        synchronized (lock) {
            int updatedRows = 0;
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE tb_users SET lastCallbackMessageID = ? WHERE userID = ?");
                preparedStatement.setLong(1, messageID);
                preparedStatement.setInt(2, userID);
                updatedRows = preparedStatement.executeUpdate();
                LoggerService.logAction(LOGTAG, getUserName(userID)
                        + "Set lastCallbackMessageID = " + messageID);
            } catch (SQLException e) {
                LoggerService.logError(LOGTAG, e);
            }
            return updatedRows > 0;
        }
    }

    private static String nvl (String value1, String value2){
        if (value1 == null){
            return value2;
        } else{
            return value1;
        }
    }







}
