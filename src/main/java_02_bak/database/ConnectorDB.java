package database;

import services.LoggerService;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConnectorDB {
    private static final String LOGTAG = "ConnectorDB";
    private static volatile Connection connection;
    private static Properties dbProperties;
    private ConnectorDB(){};

    public static Connection getConnection(){
        if (connection == null){
            synchronized (ConnectorDB.class){
                if (connection == null){
                    try{
                        String enviormentConnectionString = System.getenv("JDBC_DATABASE_URL");
                        Properties dbProperties = new Properties();
                        dbProperties.load(new FileInputStream(new File("configs/db.properties")));
                        if (enviormentConnectionString == null || enviormentConnectionString.length()<10){
                            LoggerService.logAction(LOGTAG, "Creating connection by properties file");
                            connection = DriverManager.getConnection("jdbc:"
                                    +dbProperties.getProperty("DB_TYPE")
                                    +"://"
                                    +dbProperties.getProperty("DB_HOST")
                                    +":"
                                    +dbProperties.getProperty("DB_PORT")
                                    +"/"
                                    +dbProperties.getProperty("DB_NAME")
                                    +"?user="
                                    +dbProperties.getProperty("DB_USER")
                                    +"&password="
                                    +dbProperties.getProperty("DB_PASSWORD"));

                        } else {
                            LoggerService.logAction(LOGTAG, "Creating connection by enviorment variable = " + enviormentConnectionString);
                            connection = DriverManager.getConnection(enviormentConnectionString);
                        }
                    } catch (Throwable e){
                        LoggerService.logError(LOGTAG, e);
                    }
                }
                LoggerService.logInfo(LOGTAG, "Conection is exist. Returning");
                return connection;
            }
        } else {
            LoggerService.logInfo(LOGTAG, "Conection is exist. Returning");
            return connection;
        }
    }

    @Deprecated
    public static String getTable(String tableName){
        return dbProperties.getProperty(tableName);
    }


}
