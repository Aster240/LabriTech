package application;

import db.Conn;
import dao.bookDAO;
import model.entities.Book;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        // 1. Login Simples (Pede usuario e senha do banco)
        String dbUser = JOptionPane.showInputDialog("Usuário do Banco (ex: usr_gerente):");
        String dbPass = JOptionPane.showInputDialog("Senha do Banco:");

        try {
            // 2. Conecta usando a Fábrica (Conn)
            Connection conn = Conn.getConnection(dbUser, dbPass);

            // 3. Verifica o perfil para abrir o menu correto
            String[] options = {"Funcionário", "Aluno", "Sair"};
            int choice = JOptionPane.showOptionDialog(null, "Qual seu perfil?", "LibriTech",
                    0, 3, null, options, options[0]);

            if (choice == 0) {
                // Passa a conexão para o menu do funcionário
                menuFuncionario(conn);
            } else if (choice == 1) {
                JOptionPane.showMessageDialog(null, "Menu Aluno em construção...");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro de Conexão: " + e.getMessage());
        }
    }

    private static void menuFuncionario(Connection conn) {
        // Instancia o DAO passando a conexão ativa
        bookDAO dao = new bookDAO(conn);

        String[] actions = {"1. Cadastrar Livro", "2. Realizar Empréstimo", "3. Renovar Empréstimo", "4. Excluir Livro", "Sair"};

        while(true) {
            String op = (String) JOptionPane.showInputDialog(null, "Escolha uma opção:", "Painel Funcionário",
                    1, null, actions, actions[0]);
            if (op == null || op.equals("Sair")) break;
            try {
                //  OPÇÃO 1: CADASTRAR LIVRO
                if (op.startsWith("1")) {
                    // Pede os dados na tela
                    String titulo = JOptionPane.showInputDialog("Título:");
                    String autor = JOptionPane.showInputDialog("Autor:");
                    String isbn = JOptionPane.showInputDialog("ISBN:");

                    // Converte Preço (troca virgula por ponto para evitar erro)
                    String precoStr = JOptionPane.showInputDialog("Preço (ex: 50.00):");
                    double preco = Double.parseDouble(precoStr.replace(",", "."));

                    int qtd = Integer.parseInt(JOptionPane.showInputDialog("Qtd Estoque:"));
                    Book novoLivro = new Book(titulo, autor, isbn, preco, qtd);
                    dao.cadastrarBook(novoLivro);

                    JOptionPane.showMessageDialog(null, "Livro cadastrado com sucesso!");
                }

                //opc 2: emprestimo
                else if (op.startsWith("2")) {
                    int idUser = Integer.parseInt(JOptionPane.showInputDialog("ID do Usuário:"));
                    int idLivro = Integer.parseInt(JOptionPane.showInputDialog("ID do Livro:"));

                    dao.emprestarBook(idUser, idLivro);
                    JOptionPane.showMessageDialog(null, "Empréstimo realizado!");
                }

                //opc 3: renovar
                else if (op.startsWith("3")) {
                    int idEmp = Integer.parseInt(JOptionPane.showInputDialog("ID do Empréstimo:"));
                    dao.renovar(idEmp);
                    JOptionPane.showMessageDialog(null, "Renovado por +7 dias!");
                }

                // Existing Code (Option 4 - previously Option 2)
                else if (op.startsWith("4")) {
                    String idStr = JOptionPane.showInputDialog("ID para excluir:");
                    if(idStr != null) dao.excluirBook(Integer.parseInt(idStr));
                    JOptionPane.showMessageDialog(null, "Excluído!");
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Mensagem do Banco:\n" + e.getMessage());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
            }
        }
    }

    private static void menuAluno(bookDAO dao) {
        String[] actions = {"1. Consultar Acervo", "Sair"};

        while(true) {
            String op = (String) JOptionPane.showInputDialog(null, "Área do Aluno", "Menu",
                    1, null, actions, actions[0]);

            if (op == null || op.equals("Sair")) break;

            try {
                if (op.startsWith("1")) {
                    // Busca o texto pronto do DAO
                    String lista = dao.listarAcervo();

                    // Mostra numa caixa de texto com barra de rolagem (Scroll)
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