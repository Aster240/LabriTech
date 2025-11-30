package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conn {
    private static final String URL = "jdbc:mysql://localhost:3306/libritech";

    private static Connection conn = null;

    public static Connection loginToDB(String user, String password){
        try{
            conn = DriverManager.getConnection(URL, user, password);
            return conn;
        } catch (SQLException e){
            throw new RuntimeException("Falha na entrada do SGBD, tente outra vez\n" + e.getMessage());
        }
    }
    public static Connection getConnection(){
        return conn;
    }
    public static void closeConnection(){
        if(conn != null){
            try {conn.close();}
            catch (SQLException e) {}
        }
    }
}
