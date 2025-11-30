package application;

import dao.bookDAO;
import model.entities.Book;
import javax.swing.JOptionPane;
import java.sql.SQLException;

public class MenuFuncionario {

    public static void iniciar(bookDAO dao) {
        String[] actions = {"1. Cadastrar Livro",
                "2. Realizar Empréstimo",
                "3. Renovar Empréstimo",
                "4. Devolver",
                "5. Relatorios Gerenciais",
                "6. Excluir Livro", //teste
                "Sair"};

        while(true) {
            String op = (String) JOptionPane.showInputDialog(null, "Painel Administrativo", "Menu Funcionário",
                    1, null, actions, actions[0]);

            if (op == null || op.equals("Sair")) break;

            try {
                //cadastrar
                if (op.startsWith("1")) {
                    String titulo = JOptionPane.showInputDialog("Título:");
                    String autor = JOptionPane.showInputDialog("Autor:");
                    String isbn = JOptionPane.showInputDialog("ISBN:");

                    // Tratamento simples de input
                    String precoStr = JOptionPane.showInputDialog("Preço (ex: 50.00):");
                    double preco = Double.parseDouble(precoStr.replace(",", "."));

                    int qtd = Integer.parseInt(JOptionPane.showInputDialog("Estoque:"));

                    Book novoLivro = new Book(titulo, autor, isbn, preco, qtd);
                    dao.cadastrarBook(novoLivro);
                    JOptionPane.showMessageDialog(null, "Sucesso!");
                }

                //emprestar
                else if (op.startsWith("2")) {
                    int idUser = Integer.parseInt(JOptionPane.showInputDialog("ID do Usuário:"));
                    int idLivro = Integer.parseInt(JOptionPane.showInputDialog("ID do Livro:"));
                    dao.emprestarBook(idUser, idLivro);
                    JOptionPane.showMessageDialog(null, "Empréstimo realizado!");
                }

                //renovar
                else if (op.startsWith("3")) {
                    int idEmp = Integer.parseInt(JOptionPane.showInputDialog("ID do Empréstimo:"));
                    dao.renovar(idEmp);
                    JOptionPane.showMessageDialog(null, "Renovado por +7 dias!");
                }

                // devolver
                else if (op.startsWith("4")) {
                    int idEmp = Integer.parseInt(JOptionPane.showInputDialog("ID do Empréstimo para Devolver:"));
                    dao.devolverBook(idEmp);
                    JOptionPane.showMessageDialog(null, "Livro devolvido com sucesso!");
                }

                // relatorio
                else if (op.startsWith("5")) {
                    String financeiro = dao.verDashboardFinanceiro();
                    String ranking = dao.verRankingLeitura();

                    JOptionPane.showMessageDialog(null, financeiro + "\n\n" + ranking);
                }

                // excluir, SEGURANÇA
                else if (op.startsWith("6")) {
                    String idStr = JOptionPane.showInputDialog("ID para excluir:");
                    if(idStr != null) dao.excluirBook(Integer.parseInt(idStr));
                    JOptionPane.showMessageDialog(null, "Excluído!");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Mensagem do Banco:\n" + e.getMessage());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Digite apenas números!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
            }
        }
    }
}