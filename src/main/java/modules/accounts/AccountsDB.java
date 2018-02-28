package modules.accounts;

import database.ConnectorDB;
import services.LocalizationService;
import services.LoggerService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class AccountsDB {
    private static final String LOGTAG = "AccountsDB";
    private static Connection connection;
    private static final Object lock = new Object();


    static {
        if (connection == null){
            synchronized (lock){
                connection = ConnectorDB.getConnection();
            }
        }
    }

    public static boolean createAccountsUserCursor(int userID) {
        int insertedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tb_accounts_pointer VALUES (?, 0)");
            preparedStatement.setInt(1, userID);
            insertedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
           if(setAccountUserCursor(userID, 0)){
               insertedRows = 1;
           }
        }
        return insertedRows>0;
    }
    public static int getAccountsUserCursor (int userID){
        int userCursor = 0;
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT pointer FROM tb_accounts_pointer WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                userCursor = resultSet.getInt(1);
            }

        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return userCursor;
    }
    public static boolean setAccountUserCursor (int userID, int cursor){
        int updatedRows = 0;
        if (cursor>=0 && cursor<990){
            try{
                final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE tb_accounts_pointer SET pointer = ? WHERE userID = ?");
                preparedStatement.setInt(1, cursor);
                preparedStatement.setInt(2, userID);
                updatedRows = preparedStatement.executeUpdate();
            } catch (SQLException e) {
                LoggerService.logError(LOGTAG, e);
            }
        }
        return updatedRows>0;
    }

    public static int findIndex(String request, int userID) throws AccountsException {
        int index = 0;
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("select pointer from tb_accounts_index WHERE \"group\" LIKE '"+request+"%'");
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                index = resultSet.getInt(1);
            } else {
                throw new AccountsException(LocalizationService.getString("2_AccountNotExist", userID));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LoggerService.logError(LOGTAG, e);
            throw new AccountsException(LocalizationService.getString("2_AccountNotExist", userID));
        }
        return index;

    }



    public static String getCurrentRecord(int userID){
        String answer = "";

        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM tb_accounts_index WHERE pointer = ?");
            preparedStatement.setInt(1, getAccountsUserCursor(userID));
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                answer= LocalizationService.getString("2_AccountPrefix", userID) + " " +
                        resultSet.getString(2) + " : "
                        +  AccountsService.getString(resultSet.getString(3), userID) + "\n"
                        + new String(new char[28]).replace('\0', '¯') + "\n"
                        + AccountsService.getString(resultSet.getString(2), userID);
            }

        } catch (SQLException e) {
            LoggerService.logError(LOGTAG, e);
        }
        return answer;
    }



}
