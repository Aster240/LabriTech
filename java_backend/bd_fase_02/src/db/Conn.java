package db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conn {

    private static final String URL = "jdbc:mysql://localhost:3306/libritech";
    private static Connection conn = null;

    public static Connection getConnection(String user, String password) throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                // Tenta conectar
                conn = DriverManager.getConnection(URL, user, password);
            }
            catch (SQLException e) {
                // Se der erro, joga pro Main resolver
                throw new SQLException("Erro de acesso: " + e.getMessage());
            }
        }
        return conn;
    }

    public static void fecharConexao() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}