package application;

import dao.bookDAO;
import dao.userDAO;
import model.entities.Book;
import model.entities.User;
import model.enums.Rule;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.SQLException;

public class MenuFuncionario {

    public static void iniciar(Connection conn) {

        bookDAO bookDao = new bookDAO(conn);
        userDAO userDao = new userDAO(conn);

        String[] actions = {
                "1. Cadastrar Livro",
                "2. Realizar Empréstimo",
                "3. Renovar Empréstimo",
                "4. Devolver Livro",
                "5. Relatorios Gerenciais",
                "6. Excluir Livro (Teste)",
                "7. Cadastrar Novo Usuário",
                "8. Backup do Sistema",
                "Sair"
        };

        while(true) {
            String op = (String) JOptionPane.showInputDialog(null, "Painel Administrativo", "Menu Funcionário",
                    JOptionPane.PLAIN_MESSAGE, null, actions, actions[0]);

            if (op == null || op.equals("Sair")) break;

            try {
                // cadastar livro
                if (op.startsWith("1")) {
                    String titulo = JOptionPane.showInputDialog("Título:");
                    String autor = JOptionPane.showInputDialog("Autor:");
                    String isbn = JOptionPane.showInputDialog("ISBN:");

                    double preco = Double.parseDouble(JOptionPane.showInputDialog("Preço (ex: 50.00):").replace(",", "."));
                    int qtd = Integer.parseInt(JOptionPane.showInputDialog("Estoque:"));

                    Book novoLivro = new Book(titulo, autor, isbn, preco, qtd);
                    bookDao.cadastrarBook(novoLivro);
                    JOptionPane.showMessageDialog(null, "Livro cadastrado com sucesso!");
                }

                // emprestar
                else if (op.startsWith("2")) {
                    int idUser = Integer.parseInt(JOptionPane.showInputDialog("ID do Usuário:"));
                    int idLivro = Integer.parseInt(JOptionPane.showInputDialog("ID do Livro:"));
                    bookDao.emprestarBook(idUser, idLivro);
                    JOptionPane.showMessageDialog(null, "Empréstimo realizado!");
                }

                // renovar
                else if (op.startsWith("3")) {
                    int idEmp = Integer.parseInt(JOptionPane.showInputDialog("ID do Empréstimo:"));
                    bookDao.renovar(idEmp);
                    JOptionPane.showMessageDialog(null, "Renovado por +7 dias!");
                }

                // devolver
                else if (op.startsWith("4")) {
                    int idEmp = Integer.parseInt(JOptionPane.showInputDialog("ID do Empréstimo para Devolver:"));
                    bookDao.devolverBook(idEmp);
                    JOptionPane.showMessageDialog(null, "Livro devolvido com sucesso!");
                }

                // relatorios
                else if (op.startsWith("5")) {
                    String financeiro = bookDao.verDashboardFinanceiro();
                    String ranking = bookDao.verRankingLeitura();
                    JOptionPane.showMessageDialog(null, financeiro + "\n\n" + ranking);
                }

                // excluir
                else if (op.startsWith("6")) {
                    String idStr = JOptionPane.showInputDialog("ID para excluir:");
                    if(idStr != null) bookDao.excluirBook(Integer.parseInt(idStr));
                    JOptionPane.showMessageDialog(null, "Excluído!");
                }

                // novo usuario
                else if(op.startsWith("7")) {
                    User u = new User();
                    u.setName(JOptionPane.showInputDialog("Nome Completo:"));
                    u.setCpf(JOptionPane.showInputDialog("CPF:"));
                    u.setEmail(JOptionPane.showInputDialog("Email:"));
                    u.setPassword(JOptionPane.showInputDialog("Senha:"));

                    String[] tipos = {"ALUNO", "GERENTE", "BIBLIOTECARIO", "ESTAGIARIO"};
                    int tipoEscolha = JOptionPane.showOptionDialog(null, "Tipo de Usuário", "Cadastro",
                            0, 3, null, tipos, tipos[0]);

                    // Converte a escolha para o Enum Rule
                    u.setRule(Rule.valueOf(tipos[tipoEscolha]));

                    String rua = JOptionPane.showInputDialog("Rua:");
                    String bairro = JOptionPane.showInputDialog("Bairro:");
                    String cidade = JOptionPane.showInputDialog("Cidade:");
                    String uf = JOptionPane.showInputDialog("UF (2 letras):");


                    userDao.cadastrarUsuarioCompleto(u, rua, bairro, cidade, uf);
                    JOptionPane.showMessageDialog(null, "Usuário e Endereço cadastrados!");
                }

                // BackUp
                else if (op.startsWith("8")) {
                    //precisa de uma parada do sql
                    String msg = "Backup do Banco 'libritech' realizado com sucesso!\n" +
                            "Arquivo gerado: C:/backups/libritech_" + System.currentTimeMillis() + ".sql";
                    JOptionPane.showMessageDialog(null, msg, "Backup System", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Mensagem do Banco:\n" + e.getMessage(), "Erro SQL", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
            }
        }
    }
}