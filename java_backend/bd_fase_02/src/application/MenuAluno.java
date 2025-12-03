package application;

import dao.BookDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class MenuAluno {

    public static void iniciar(BookDAO dao) {
        String[] actions = {"1. Consultar Acervo", "2. Menu Empréstimo" ,"Sair"};

        while(true) {
            String op = (String) JOptionPane.showInputDialog(null, "Área do Aluno", "Menu Aluno",
                    1, null, actions, actions[0]);

            if (op == null || op.equals("Sair")) break;

            try {
                if (op.startsWith("1")) {
                    DefaultTableModel modelo = dao.listarAcervoTabela();
                    JTable tabela = new JTable(modelo);
                    JScrollPane scroll = new JScrollPane(tabela);
                    scroll.setPreferredSize(new Dimension(600, 300)); // Define o tamanho da janela
                    JOptionPane.showMessageDialog(null, scroll, "Acervo Disponível", JOptionPane.PLAIN_MESSAGE);
                }

                else if (op.startsWith("2")) {
                    String idStr = JOptionPane.showInputDialog("Digite o seu ID de Aluno:");
                    if(idStr != null && !idStr.isEmpty()) {
                        String meusLivros = dao.verMeusEmprestimos(Integer.parseInt(idStr));
                        exibirEmScroll(meusLivros);
                    }
                }

            }catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Erro ao buscar livros: " + e.getMessage());
            }
        }
    }
    public static void exibirEmScroll(String texto){
        javax.swing.JTextArea area = new javax.swing.JTextArea(texto);
        area.setRows(10);
        area.setColumns(30);
        area.setEditable(false);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));

        JOptionPane.showMessageDialog(null, scrollPane, "Resultado", JOptionPane.INFORMATION_MESSAGE);

    }
}
