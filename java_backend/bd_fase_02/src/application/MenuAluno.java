package application;

import dao.bookDAO;
import javax.swing.JOptionPane;
import java.sql.SQLException;

public class MenuAluno {

    public static void iniciar(bookDAO dao) {
        String[] actions = {"1. Consultar Acervo", "Sair"};

        while(true) {
            String op = (String) JOptionPane.showInputDialog(null, "Área do Aluno", "Menu Aluno",
                    1, null, actions, actions[0]);

            if (op == null || op.equals("Sair")) break;

            try {
                if (op.startsWith("1")) {
                    String lista = dao.listarAcervo();

                    javax.swing.JTextArea area = new javax.swing.JTextArea(lista);
                    area.setEditable(false);
                    javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(area);
                    scroll.setPreferredSize(new java.awt.Dimension(400, 300));

                    JOptionPane.showMessageDialog(null, scroll);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Erro ao buscar livros: " + e.getMessage());
            }
        }
    }
}