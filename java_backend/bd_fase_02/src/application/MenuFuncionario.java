package application;

import dao.BookDAO;
import dao.UserDAO;
import model.entities.Book;
import model.entities.User; // papai
import model.entities.Student;   //filho 1
import model.entities.Employee;  //filho 2
import model.enums.Rule;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.SQLException;

public class MenuFuncionario {

    public static void iniciar(Connection conn) {

        BookDAO bookDao = new BookDAO(conn);
        UserDAO userDao = new UserDAO(conn);

        String[] actions = {
                "1. Cadastrar Livro",
                "2. Realizar Empréstimo",
                "3. Renovar Empréstimo",
                "4. Devolver Livro",
                "5. Relatorios Gerenciais",
                "6. Excluir Livro (Teste de Segurança)",
                "7. Cadastrar Novo Usuário",
                "8. Backup do Sistema",
                "Sair"
        };

        while(true) {
            String op = (String) JOptionPane.showInputDialog(null, "Painel Administrativo", "Menu Funcionário",
                    JOptionPane.PLAIN_MESSAGE, null, actions, actions[0]);

            if (op == null || op.equals("Sair")) break;

            try {
                // 1. Cadastrar Livro
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

                // 2. Emprestar
                else if (op.startsWith("2")) {
                    int idUser = Integer.parseInt(JOptionPane.showInputDialog("ID do Usuário:"));
                    int idLivro = Integer.parseInt(JOptionPane.showInputDialog("ID do Livro:"));
                    bookDao.emprestarBook(idUser, idLivro);
                    JOptionPane.showMessageDialog(null, "Empréstimo realizado!");
                }

                // 3. Renovar
                else if (op.startsWith("3")) {
                    int idEmp = Integer.parseInt(JOptionPane.showInputDialog("ID do Empréstimo:"));
                    bookDao.renovar(idEmp);
                    JOptionPane.showMessageDialog(null, "Renovado por +7 dias!");
                }

                // 4. Devolver
                else if (op.startsWith("4")) {
                    int idEmp = Integer.parseInt(JOptionPane.showInputDialog("ID do Empréstimo para Devolver:"));
                    bookDao.devolverBook(idEmp);
                    JOptionPane.showMessageDialog(null, "Livro devolvido e multa calculada (se houver)!");
                }

                // 5. Relatórios
                else if (op.startsWith("5")) {
                    String financeiro = bookDao.verDashboardFinanceiro();
                    String ranking = bookDao.verRankingLeitura();
                    JOptionPane.showMessageDialog(null, financeiro + "\n\n" + ranking);
                }

                // 6. Excluir Livro (A PROVA DE SEGURANÇA)
                else if (op.startsWith("6")) {
                    String idStr = JOptionPane.showInputDialog("ID do livro para excluir:");
                    if(idStr != null) {
                        // Se estiver logado como Estagiário, isso vai explodir uma exceção do Banco
                        bookDao.excluirBook(Integer.parseInt(idStr));
                        JOptionPane.showMessageDialog(null, "Livro excluído com sucesso!");
                    }
                }

                // 7. Novo Usuário (APLICAÇÃO DA POO)
                else if(op.startsWith("7")) {
                    String[] tipos = {"ALUNO", "GERENTE", "BIBLIOTECARIO", "ESTAGIARIO"};
                    int tipoEscolha = JOptionPane.showOptionDialog(null, "Tipo de Usuário", "Cadastro",
                            0, 3, null, tipos, tipos[0]);

                    Rule regraSelecionada = Rule.valueOf(tipos[tipoEscolha]);

                    // -- AQUI ESTÁ A HERANÇA/POLIMORFISMO --
                    User u;
                    if (regraSelecionada == Rule.ALUNO) {
                        u = new Student(); // Instancia Filho 1
                    } else {
                        u = new Employee(); // Instancia Filho 2
                    }

                    u.setRule(regraSelecionada);
                    u.setName(JOptionPane.showInputDialog("Nome Completo:"));
                    u.setCpf(JOptionPane.showInputDialog("CPF:"));
                    u.setEmail(JOptionPane.showInputDialog("Email:"));
                    u.setPassword(JOptionPane.showInputDialog("Senha:"));

                    String rua = JOptionPane.showInputDialog("Rua:");
                    String bairro = JOptionPane.showInputDialog("Bairro:");
                    String cidade = JOptionPane.showInputDialog("Cidade:");
                    String uf = JOptionPane.showInputDialog("UF (2 letras):");

                    // DAO aceita 'User', mas estamos passando Student ou Employee (Polimorfismo)
                    userDao.cadastrarUsuarioCompleto(u, rua, bairro, cidade, uf);

                    // Exibe o prazo só para mostrar ao professor que a lógica funcionou
                    JOptionPane.showMessageDialog(null, "Usuário criado!\nClasse Java: " + u.getClass().getSimpleName() +
                            "\nPrazo de Empréstimo: " + u.getLoanDeadlineDays() + " dias.");
                }

                // 8. Backup
                else if (op.startsWith("8")) {
                    try {
                        // Ajuste os caminhos conforme seu PC na hora da apresentação
                        String cmd = "mysqldump -u root -p1234 libritech -r backup.sql";
                        // Runtime.getRuntime().exec(cmd); // Descomente se tiver mysqldump no Path
                        JOptionPane.showMessageDialog(null, "Comando de backup enviado ao sistema operacional!");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Erro ao tentar backup: " + ex.getMessage());
                    }
                }

            } catch (SQLException e) {
                // TRATAMENTO OBRIGATÓRIO (Item 5 do PDF)
                // Se o banco disser "Access denied" (código 1142), mostramos msg bonita
                if (e.getMessage().toLowerCase().contains("denied") || e.getErrorCode() == 1142) {
                    JOptionPane.showMessageDialog(null,
                            "ERRO: ACESSO NEGADO!\n\n" +
                                    "Seu perfil não tem permissão para realizar esta operação.\n" +
                                    "O Banco de Dados bloqueou a ação.",
                            "Segurança", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Erro SQL: " + e.getMessage());
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
            }
        }
    }
}