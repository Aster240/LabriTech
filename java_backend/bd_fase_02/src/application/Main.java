package application;

import db.Conn;
import dao.BookDAO;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        boolean rodando = true;

        while (rodando) {
            // 1. TELA DE LOGIN
            String dbUser = JOptionPane.showInputDialog(null,
                    "=== LIBRITECH LOGIN ===\n\nUsuário do Banco (ex: usr_gerente, usr_aluno):",
                    "Login", JOptionPane.QUESTION_MESSAGE);
            if (dbUser == null) {
                rodando = false;
                break;
            }
            String dbPass = JOptionPane.showInputDialog(null, "Senha:", "Login", JOptionPane.QUESTION_MESSAGE);
            if (dbPass == null) {
                rodando = false;
                break;
            }
            Connection conn = null;

            try {
                // 2. Tenta Conectar
                conn = Conn.getConnection(dbUser, dbPass);

                BookDAO dao = new BookDAO(conn);

                String perfilTexto = "Visitante";
                if (dbUser.contains("gerente") || dbUser.contains("biblio") || dbUser.contains("estag")) {
                    perfilTexto = "Administrativo";
                } else if (dbUser.contains("aluno")) {
                    perfilTexto = "Aluno";
                }
                boolean logado = true;
                while (logado) {
                    String[] options = {"Acesso Funcionário", "Acesso Aluno", "Logout (Sair)"};
                    int choice = JOptionPane.showOptionDialog(null,
                            "Bem-vindo ao LibriTech v2.0\n\n" +
                                    "Logado como: " + dbUser + " (" + perfilTexto + ")\n" +
                                    "Selecione seu painel:",
                            "Menu Principal",
                            0, 3, null, options, options[0]);

                    if (choice == 0) {
                        try {
                            MenuFuncionario.iniciar(conn);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, "Erro ao abrir menu: " + e.getMessage());
                        }
                    } else if (choice == 1) {
                        try {
                            MenuAluno.iniciar(dao);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, "Erro ao abrir menu: " + e.getMessage());
                        }
                    } else {
                        logado = false;
                        JOptionPane.showMessageDialog(null, "Fazendo Logout...", "Saindo", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Falha no Login!\nVerifique usuário e senha.\n\nErro: " + e.getMessage(),
                        "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro crítico: " + e.getMessage());
            } finally {
                Conn.fecharConexao();
            }
        }
        System.exit(0);
    }
}