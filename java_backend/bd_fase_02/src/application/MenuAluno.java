package application;

import dao.BookDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class MenuAluno {

    public static void iniciar(BookDAO dao) {
        String[] actions = {"1. Consultar Acervo (Tabela)", "2. Meus Empréstimos", "Voltar"};

        while(true) {
            String op = (String) JOptionPane.showInputDialog(null, "Área do Aluno", "Menu Aluno",
                    JOptionPane.QUESTION_MESSAGE, null, actions, actions[0]);

            if (op == null || op.equals("Voltar")) break;

            try {
                if (op.startsWith("1")) {
                    DefaultTableModel model = dao.listarAcervoTabela();
                    JTable tabela = new JTable(model);
                    tabela.setFillsViewportHeight(true);
                    JScrollPane scroll = new JScrollPane(tabela);
                    scroll.setPreferredSize(new Dimension(500, 300));

                    JOptionPane.showMessageDialog(null, scroll, "Acervo Disponível", JOptionPane.PLAIN_MESSAGE);
                }
                else if (op.startsWith("2")) {
                    // Se clicar em Cancelar, retorna null
                    String idStr = JOptionPane.showInputDialog("Confirme seu ID de Aluno:");
                    if (idStr != null && !idStr.trim().isEmpty()) {
                        String historico = dao.verMeusEmprestimos(Integer.parseInt(idStr));
                        exibirTexto(historico);
                    } else if (idStr != null) {
                        JOptionPane.showMessageDialog(null, "Digite um ID válido.");
                    }
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Erro no banco: " + e.getMessage());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "O ID deve ser numérico!");
            }
        }
    }

    private static void exibirTexto(String texto) {
        JTextArea area = new JTextArea(texto);
        area.setRows(15);
        area.setColumns(40);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(area);
        JOptionPane.showMessageDialog(null, scroll);
    }
}