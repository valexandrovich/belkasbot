package modules.bankrupts;

import database.ConnectorDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

public class BankruptsBD {
    private static final String LOGTAG = "BankruptsDB";
    private static Connection connection;
    private static final Object lock = new Object();


    static {
        if (connection == null){
            synchronized (lock){
                connection = ConnectorDB.getConnection();
            }
        }
    }

    public static boolean addUserSession (LinkedList<BankruptRow> responseList, int userID){
      int insertedRows = 0;
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tb_bankrupts_session VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for (BankruptRow row :responseList){
                preparedStatement.setInt(1, userID);
                preparedStatement.setString(2, row.getPublicDate());
                preparedStatement.setString(3, row.getNumber());
                preparedStatement.setString(4, row.getDocType());
                preparedStatement.setString(5, row.getOkpo());
                preparedStatement.setString(6, row.getName());
                preparedStatement.setString(7, row.getDocNumber());
                preparedStatement.setString(8, row.getCourtName());
                preparedStatement.setString(9, row.getAuDate());
                preparedStatement.setString(10, row.getFinalDate());
                insertedRows+= preparedStatement.executeUpdate();
                setUserPointer(userID, 0);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return insertedRows>0;
    }
    public static boolean clearUserSession (int userID){
        int deletedRows = 0;
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tb_bankrupts_session WHERE  userID = ?");
            preparedStatement.setInt(1, userID);
            deletedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deletedRows>0;
    }

    public static int getSessionRowsCount (int userID){
        int rowsCount = 0;
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM tb_bankrupts_session");
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                rowsCount = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowsCount;
    }

    public static boolean setUserPointer (int userID, int pointer){
        int updatedRows = 0;
        int maxRows = getSessionRowsCount(userID);

        if (pointer>=0 && pointer<maxRows) {
            try {
                final PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM tb_bankrupts_cursor WHERE userID = ?");
                preparedStatement.setInt(1, userID);
                final ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    final PreparedStatement preparedStatement1 = connection.prepareStatement("UPDATE tb_bankrupts_cursor SET pointer = ? WHERE userID = ?");
                    preparedStatement1.setInt(1, pointer);
                    preparedStatement1.setInt(2, userID);
                    updatedRows = preparedStatement1.executeUpdate();
                } else {
                    final PreparedStatement preparedStatement2 = connection.prepareStatement("INSERT INTO tb_bankrupts_cursor VALUES (?, ?, ?)");
                    preparedStatement2.setInt(1, userID);
                    preparedStatement2.setInt(2, pointer);
                    preparedStatement2.setInt(3, maxRows);
                    updatedRows = preparedStatement2.executeUpdate();
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return updatedRows>0;

    }
    public static int getUserPointer(int userID){
        int userPointer = 0;
        try{
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT pointer FROM tb_bankrupts_cursor WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                userPointer = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userPointer;
    }

    public static BankruptRow getCurrentBankruptRow(int userID){
        BankruptRow row;
        int cursor = getUserPointer(userID);
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM tb_bankrupts_session WHERE userID = ?");
            preparedStatement.setInt(1, userID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                resultSet.beforeFirst();
                for (int i = 0; i <= cursor ; i++) {
                    resultSet.next();
                }

                //  publicDate,  number,  docType,  okpo,  name,  docNumber,  courtName,  auDate,  finalDate
                row = new BankruptRow(resultSet.getString(2)
                        ,resultSet.getString(3)
                        ,resultSet.getString(4)
                        ,resultSet.getString(5)
                        ,resultSet.getString(6)
                        ,resultSet.getString(7)
                        ,resultSet.getString(8)
                        ,resultSet.getString(9)
                        ,resultSet.getString(10));

                return row;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }


}
