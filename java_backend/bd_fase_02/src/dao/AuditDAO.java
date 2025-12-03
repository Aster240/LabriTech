package dao;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditDAO {

    private Connection conexao;

    public AuditDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public DefaultTableModel listarLogs() throws SQLException {

        String sql = "SELECT * FROM log_auditoria ORDER BY id_log DESC";

        DefaultTableModel modelo = new DefaultTableModel();

        modelo.addColumn("ID Log");
        modelo.addColumn("Tabela");
        modelo.addColumn("Ação");
        modelo.addColumn("Usuário");
        modelo.addColumn("Dados Antigos");
        modelo.addColumn("Data/Hora");

        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("id_log"),
                        rs.getString("tabela_afetada"),
                        rs.getString("acao"),
                        rs.getString("usuario_responsavel"),
                        rs.getString("dados_antigos"),
                        rs.getTimestamp("data_hora")
                });
            }
        }
        return modelo;
    }
}