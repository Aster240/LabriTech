package application;

import dao.bookDAO;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class MenuAluno {

    public static void iniciar(bookDAO dao) {
        String[] actions = {"1. Consultar Acervo", "2. Menu Empréstimo" ,"Sair"};

        while(true) {
            String op = (String) JOptionPane.showInputDialog(null, "Área do Aluno", "Menu Aluno",
                    1, null, actions, actions[0]);

            if (op == null || op.equals("Sair")) break;

            try {
                if (op.startsWith("1")) {
                    String lista = dao.listarAcervo();

                    JTextArea area = new JTextArea(lista);
                    area.setEditable(false);
                    JScrollPane scroll = new JScrollPane(area);
                    scroll.setPreferredSize(new Dimension(400, 300));

                    JOptionPane.showMessageDialog(null, scroll);
                }
                else if (op.startsWith("2")) {
                    //login do banco é genérico, pede um id de aluno
                    String idStr = JOptionPane.showInputDialog("Digite o ID do Aluno");
                    if(idStr != null) {
                        int id = Integer.parseInt(idStr);
                        String meusLivros = dao.verMeusEmprestimos(id);
                        exibirEmScroll(meusLivros);
                    }
                }

            } catch (SQLException e) {
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
