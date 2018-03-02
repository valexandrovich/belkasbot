package modules.rates;

import database.ConnectorDB;
import services.LoggerService;

import java.sql.*;
import java.util.ArrayList;

public class RatesDB {
    private static final String LOGTAG = "RatesDB";
    private static volatile Connection connection;
    private static ArrayList<String> defaultCurrencies = new ArrayList<>();

    // Due to Singletone
    private RatesDB(){}

    static {
        if (connection == null) {
            connection = ConnectorDB.getConnection();
            LoggerService.logInfo(LOGTAG, "Initialize connection");
        }

        if (defaultCurrencies.size() == 0){
            defaultCurrencies.add("USD");
            defaultCurrencies.add("EUR");
            defaultCurrencies.add("CHF");
            defaultCurrencies.add("RUB");
        }

    }

    public static ArrayList<String> getUserCurrencies(int userID){
        ArrayList<String> userCurrencies = new ArrayList<>();
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT ccy FROM tb_rates_user_currencies WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                userCurrencies.add(resultSet.getString(1));
            }

        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return userCurrencies;
    }
    public static boolean removeUserCurrency(int userID, String ccy){
        int removedRows = 0;
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tb_rates_user_currencies WHERE userID = ? AND ccy = ?");
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, ccy);
            removedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
           LoggerService.logError(LOGTAG, e);
        }
        return removedRows>0;
    }
    public static boolean addUserCurrency(int userID, String ccy){
        int addedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tb_rates_user_currencies VALUES (?, ?)");
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, ccy);
            addedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return addedRows>0;
    }

    public static boolean initializeDefaulCurrenciesForUser(int userID){
        int insertedRows = 0;
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tb_rates_user_currencies VALUES (?, ?)");
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, "USD");
            insertedRows +=  preparedStatement.executeUpdate();

            preparedStatement.setString(2, "EUR");
            insertedRows += preparedStatement.executeUpdate();

            preparedStatement.setString(2, "CHF");
            insertedRows += preparedStatement.executeUpdate();

            preparedStatement.setString(2, "RUB");
            insertedRows += preparedStatement.executeUpdate();

            preparedStatement.setString(2, "PLN");
            insertedRows += preparedStatement.executeUpdate();

            preparedStatement.setString(2, "HUF");
            insertedRows += preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return insertedRows>0;
    }

    public static String getCurrentUserEmail(int userID){
        String currentEmail = "";
        int currentCursor = getEmailCursor(userID);
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT email FROM tb_rates_user_email WHERE userID = ? ORDER by insertdate DESC");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();

            for (int i = 0; i <= currentCursor ; i++) {
                resultSet.next();
            }
            currentEmail = resultSet.getString(1);

        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }

        return currentEmail;
    }
    public static ArrayList<String> getUserEmails (int userID){
        ArrayList<String> userEmails = new ArrayList<>();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT email FROM tb_rates_user_email WHERE userID = ? ORDER BY insertdate DESC");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                userEmails.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return userEmails;
    }
    public static int getEmailsCount (int userID){
        int userEmailsCount = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT count(email) FROM tb_rates_user_email WHERE userID = ? ");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                userEmailsCount = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return userEmailsCount;
    }
    public static boolean addUserEmail (int userID, String email){
        int addedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT  INTO tb_rates_user_email VALUES (?, ?, ?)");
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, email);
            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            addedRows = preparedStatement.executeUpdate();
            // Setting 0 - pointer if first email
            if (getEmailsCount(userID) == 1){
                setEmailCursor(userID, 0);
            }
            /////////////////////////////////////
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return addedRows>0;
    }
    public static boolean removeUserEmail(int userID, String email){
        int deletedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tb_rates_user_email WHERE userID = ? AND email = ?");
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, email);
            deletedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deletedRows>0;
    }


    public static boolean setEmailCursor(int userID, int cursor){
        int addedRows = 0;
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tb_rates_email_cursor WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            preparedStatement.executeUpdate();

            preparedStatement = null;
            preparedStatement = connection.prepareStatement("INSERT INTO tb_rates_email_cursor VALUES (?, ?)");
            preparedStatement.setInt(1, userID);
            preparedStatement.setInt(2, cursor);
            addedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return  addedRows>0;
    }
    public static int getEmailCursor(int userID){
        int cursor = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT pointer FROM tb_rates_email_cursor WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                cursor = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cursor;
    }
    public static boolean incrementEmailCursor(int userID){
        boolean isIncrement = false;
        int currentCursor = getEmailCursor(userID);
        if (currentCursor+1<getEmailsCount(userID)){
            setEmailCursor(userID, currentCursor+1);
            isIncrement = true;
        }
        return isIncrement;

    }
    public static boolean decrementEmailCursor(int userID){
        boolean isDecrement = false;
        int currentCursor = getEmailCursor(userID);
        if (currentCursor>0){
            setEmailCursor(userID, currentCursor-1);
            isDecrement = true;
        }
        return isDecrement;
    }


    public static boolean addRatesResponce(int userID, Date rateDate){
        int insertedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tb_rates_responce VALUES (?, ?, ?)");
            preparedStatement.setInt(1, userID);
            preparedStatement.setTimestamp(2, new Timestamp(rateDate.getTime()));
            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            insertedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return insertedRows>0;
    }
    public static Date getLastRateDate (int userID){
        Date lastRateDate = new Date(System.currentTimeMillis());
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT ratedate FROM tb_rates_response WHERE userid = ? ORDER BY insertdate DESC");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                lastRateDate = resultSet.getDate(1);
            }
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return lastRateDate;
    }

}
