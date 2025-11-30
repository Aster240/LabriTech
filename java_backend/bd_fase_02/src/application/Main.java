package application;

import db.Conn;
import dao.bookDAO;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        String dbUser = JOptionPane.showInputDialog("Usuário do Banco (ex: usr_gerente):");
        String dbPass = JOptionPane.showInputDialog("Senha do Banco:");

        Connection conn = null;

        try {
            // 1. Conecta
            conn = Conn.getConnection(dbUser, dbPass);
            bookDAO dao = new bookDAO(conn);

            // 3. Escolhe o perfil
            String[] options = {"Funcionário", "Aluno", "Sair"};
            int choice = JOptionPane.showOptionDialog(null, "Sistema LibriTech\nPerfil: " + dbUser, "Login",
                    0, 3, null, options, options[0]);

            // 4. Redireciona para a classe correta
            if (choice == 0) {
                MenuFuncionario.iniciar(dao);
            } else if (choice == 1) {
                MenuAluno.iniciar(dao);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro de Conexão: " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro fatal: " + e.getMessage());
        } finally {
            Conn.fecharConexao();
        }
    }
}