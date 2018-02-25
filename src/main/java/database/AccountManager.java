package database;

import org.telegram.telegrambots.api.objects.User;
import services.LoggerService;

import java.sql.*;

public class AccountManager {
    private static final String LOGTAG = "AccountManager";

    private static volatile Connection connection;

    private AccountManager(){};

    static {
        LoggerService.logInfo(LOGTAG, "Initialize connection");
        connection = ConnectorDB.getConnection();
    }


    public static boolean isSessionExist(int userID){
        boolean exist = false;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT userID FROM tb_users WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                exist = true;
                LoggerService.logInfo(LOGTAG, userID + " user exist");
            }
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return exist;
    }
    public static boolean createUser(User user){
        int updatedRows = 0;
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tb_users VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, user.getId());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setBoolean(4, user.getBot());
            preparedStatement.setString(5, user.getUserName());
            preparedStatement.setString(6,  nvl(user.getLanguageCode(), "ru-ru"));
            preparedStatement.setLong(7, 0);
            preparedStatement.setInt(8, 0);
            preparedStatement.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setLong(10, 0);
            updatedRows = preparedStatement.executeUpdate();
            LoggerService.logAction(LOGTAG,  user.getId() + " " + user.getFirstName() + " " + user.getLastName() + " creating user");
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return updatedRows>0;
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
            if (updatedRows>0) {LoggerService.logAction(LOGTAG, "Change user " + userID + " state to " + state);}
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
            if (updatedRows>0) {LoggerService.logAction(LOGTAG, "Change user " + userID + " languageCode to " + languageCode);}
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
            e.printStackTrace();
        }
        return lastCallbackMessageID;
    }
    public static boolean setLastCallbackMessageID(int userID, long messageID){
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE tb_users SET lastCallbackMessageID = ? WHERE userID = ?");
            preparedStatement.setLong(1, messageID);
            preparedStatement.setInt(2, userID);
            updatedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedRows>0;
    }

    private static String nvl (String value1, String value2){
        if (value1 == null){
            return value2;
        } else{
            return value1;
        }
    }







}
