package modules.rates;

import database.ConnectorDB;
import services.LoggerService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class RatesDB {
    private static final String LOGTAG = "RatesDB";
    private static volatile Connection connection;
    private static final SimpleDateFormat shortDate = new SimpleDateFormat("dd.MM.yyyy");
    private static ArrayList<String> defaultCurrencies = new ArrayList<>();

    private RatesDB(){}

    static {
        LoggerService.logInfo(LOGTAG, "initialize connection");
        defaultCurrencies.add("USD");
        defaultCurrencies.add("EUR");
        defaultCurrencies.add("CHF");
        defaultCurrencies.add("RUB");
        if (connection == null) {
            connection  = ConnectorDB.getConnection();
        }
    }


    public static boolean addUserCurrency(int userID, String ccy){
        int insertedRows = 0;
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tb_rates_user_currencies VALUES (? , ?)");
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, ccy);
            insertedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return insertedRows>0;
    }
    public static boolean removeUserCurrency(int userID, String ccy){
        int insertedRows = 0;
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tb_rates_user_currencies WHERE userID = ? AND ccy = ?");
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, ccy);
            insertedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return insertedRows>0;
    }
    public static ArrayList<String> getUserCurrencies(int userID){
        ArrayList<String> userCurrencies = new ArrayList<>();
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT ccy FROM tb_rates_user_currencies WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                userCurrencies.add(resultSet.getString(1));
            }
            if (userCurrencies.size()<1){
                setDefaultCurrencies(userID);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()){
                    userCurrencies.add(resultSet.getString(1));
                }
            }
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return userCurrencies;
    }
    private static boolean setDefaultCurrencies(int userID){
        int insertedRows = 0;
        try {
            if (defaultCurrencies.size() > 0) {
                final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tb_rates_user_currencies VALUES (? , ?)");
                preparedStatement.setInt(1, userID);
                for (String ccy : defaultCurrencies){
                    preparedStatement.setString(2, ccy);
                    insertedRows+=preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return insertedRows>0;
    }


    public static boolean addRatesResponce(int userID, Timestamp rateDate){
        int insertedRow = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tb_rates_response VALUES (?, ?, ?)");
            preparedStatement.setInt(1, userID);
            preparedStatement.setTimestamp(2, rateDate);
            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            insertedRow = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return insertedRow>0;
    }
    public static Timestamp getLastResponseDate(int userID){
        Timestamp lastDate = new Timestamp(System.currentTimeMillis());
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT rateDate FROM tb_rates_response WHERE userID = ? ORDER BY insertDate DESC");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                lastDate = resultSet.getTimestamp(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastDate;
    }

    // region Email
        public static String getCurrentEmail(int userID) {
        String userEmail = null;
        ArrayList<String> userEmails = new ArrayList<>();
        try {
                final PreparedStatement preparedStatement = connection.prepareStatement("SELECT email FROM tb_rates_user_email WHERE userID = ? GROUP BY userID, email, insertdate ORDER BY insertDate DESC");
                preparedStatement.setInt(1, userID);
                final ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()){
                    userEmails.add(resultSet.getString(1));
                }

                if (userEmails.size()<1){
                    return userEmail;
                } else if (userEmails.size() == 1){
                    return userEmails.get(0);
                }
                userEmail = userEmails.get(getEmailCursor(userID));

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return userEmail;
        }
        public static boolean addUserEmail(int userID, String email){
        int insertedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tb_rates_user_email VALUES (?, ?, ?)");
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, email);
            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            insertedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


            setEmailCursor(userID, 0);


        return insertedRows>0;
    }
        public static boolean removeUserEmail(int userID){
            String currentEmail = getCurrentEmail(userID);
            int deletedRows = 0;
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tb_rates_user_email WHERE userID = ? AND email = ?");
                preparedStatement.setInt(1, userID);
                preparedStatement.setString(2, currentEmail);
                deletedRows = preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return deletedRows>0;

        }
        @Deprecated
        public static ArrayList<String> getUserEmails(int userID){
        ArrayList<String> userEmails = new ArrayList<>();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT email FROM tb_user_email WHERE userID = ? ORDER BY insertDate DESC");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                userEmails.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userEmails;

    }
        public static int getEmailCursor(int userID){
            int emailCursor = 0;
            try{
                final PreparedStatement preparedStatement = connection.prepareStatement("SELECT pointer FROM tb_rates_email_cursor WHERE userID = ?");
                preparedStatement.setInt(1, userID);
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next())
                {
                    emailCursor = resultSet.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return emailCursor;
        }
        public static boolean setEmailCursor(int userID, int cursor){
            int updatedRows = 0;
            int cursorSet = Math.max(cursor, 0);
            cursorSet = Math.min(cursorSet, getEmailsCount(userID));

            try {
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tb_rates_email_cursor WHERE userID = ?");
                preparedStatement.setInt(1, userID);
                preparedStatement.executeUpdate();

                preparedStatement = connection.prepareStatement("INSERT INTO tb_rates_email_cursor VALUES (?, ?)");
                preparedStatement.setInt(1, userID);
                preparedStatement.setInt(2, cursorSet);
                updatedRows = preparedStatement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return updatedRows>0;
        }
        public static int getEmailsCount(int userID) {
            int emailsCount = 0;
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(email)FROM tb_rates_user_email WHERE userID = ? ORDER BY insertDate DESC");
                preparedStatement.setInt(1, userID);
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()){
                    emailsCount = resultSet.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return emailsCount;
        }
        public static boolean incrementCursor(int userID){
            boolean answer = false;
            int currentCursor = getEmailCursor(userID);
            if ((currentCursor+1)<getEmailsCount(userID)){
                currentCursor++;
                answer = true;
            }
            setEmailCursor(userID, currentCursor);
            return answer;
        }
        public static boolean decrementCursor(int userID){
            boolean answer = false;
            int currentCursor = getEmailCursor(userID);
            if (currentCursor >0){
                currentCursor--;
                answer = true;
            }
            setEmailCursor(userID, currentCursor);
            return answer;
        }




    // endregion

}
