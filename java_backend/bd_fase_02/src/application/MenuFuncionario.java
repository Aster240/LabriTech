package application;

import dao.BookDAO;
import dao.UserDAO;
import model.entities.Book;
import model.entities.User;
import model.entities.Student;
import model.entities.Employee;
import model.enums.Rule;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class MenuFuncionario {

    // (Cancelamento)
    private static class OperacaoCanceladaException extends RuntimeException {}

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
                "9. Listar livros",
                "Voltar"
        };

        while(true) {
            String op = (String) JOptionPane.showInputDialog(null, "Painel Administrativo", "Menu Funcionário",
                    JOptionPane.PLAIN_MESSAGE, null, actions, actions[0]);

            if (op == null || op.equals("Voltar")) break;

            try {
                // 1. Cadastrar Livro
                if (op.startsWith("1")) {
                    String titulo = pedir("Título do Livro:");
                    String autor = pedir("Autor:");
                    String isbn = pedir("ISBN:");

                    // Tratamento de vírgula para ponto no preço
                    double preco = Double.parseDouble(pedir("Preço (ex: 59.90):").replace(",", "."));
                    int qtd = Integer.parseInt(pedir("Quantidade em Estoque:"));

                    Book novoLivro = new Book(titulo, autor, isbn, preco, qtd);
                    bookDao.cadastrarBook(novoLivro);
                    JOptionPane.showMessageDialog(null, "Livro cadastrado com sucesso!");
                }

                // 2. Emprestar
                else if (op.startsWith("2")) {
                    int idUser = Integer.parseInt(pedir("ID do Usuário:"));
                    int idLivro = Integer.parseInt(pedir("ID do Livro:"));

                    // Lógica simples de prazo para apresentação
                    String[] tipos = {"Aluno (7 dias)", "Funcionário (14 dias)"};
                    int escolha = JOptionPane.showOptionDialog(null, "Quem está pegando o livro?", "Regra de Prazo",
                            0, 3, null, tipos, tipos[0]);
                    if (escolha == -1) throw new OperacaoCanceladaException(); // Se fechar a janela de escolha

                    int dias = (escolha == 0) ? 7 : 14;

                    bookDao.emprestarBook(idUser, idLivro, dias);
                    JOptionPane.showMessageDialog(null, "Empréstimo realizado!");
                }

                // 3. Renovar
                else if (op.startsWith("3")) {
                    int idEmp = Integer.parseInt(pedir("ID do Empréstimo para Renovar:"));
                    bookDao.renovar(idEmp);
                    JOptionPane.showMessageDialog(null, "Renovado por +7 dias!");
                }

                // 4. Devolver
                else if (op.startsWith("4")) {
                    int idEmp = Integer.parseInt(pedir("ID do Empréstimo para Devolver:"));
                    bookDao.devolverBook(idEmp);
                    JOptionPane.showMessageDialog(null, "Livro devolvido e multa calculada (se houver)!");
                }

                // 5. Relatórios
                else if (op.startsWith("5")) {
                    String financeiro = bookDao.verDashboardFinanceiro();
                    String ranking = bookDao.verRankingLeitura();
                    JOptionPane.showMessageDialog(null, financeiro + "\n\n" + ranking);
                }

                // 6. Excluir
                else if (op.startsWith("6")) {
                    String idStr = pedir("ID do livro para excluir:");
                    bookDao.excluirBook(Integer.parseInt(idStr));
                    JOptionPane.showMessageDialog(null, "Livro excluído com sucesso!");
                }

                // 7. Novo Usuário
                else if(op.startsWith("7")) {
                    String[] tipos = {"ALUNO", "GERENTE", "BIBLIOTECARIO", "ESTAGIARIO"};
                    int tipoEscolha = JOptionPane.showOptionDialog(null, "Tipo de Usuário", "Cadastro",
                            0, 3, null, tipos, tipos[0]);

                    if (tipoEscolha == -1) throw new OperacaoCanceladaException();
                    Rule regraSelecionada = Rule.valueOf(tipos[tipoEscolha]);
                    User u;
                    if (regraSelecionada == Rule.ALUNO) {
                        u = new Student();
                    } else {
                        u = new Employee();
                    }

                    u.setRule(regraSelecionada);
                    u.setName(pedir("Nome Completo:"));
                    u.setCpf(pedir("CPF:"));
                    u.setEmail(pedir("Email:"));
                    u.setPassword(pedir("Senha:"));


                    String rua = pedir("Rua:");
                    String bairro = pedir("Bairro:");
                    String cidade = pedir("Cidade:");
                    String uf = pedir("UF (2 letras):");


                    if (rua == null || bairro == null || cidade == null || uf == null ||
                            rua.isBlank() || bairro.isBlank() || cidade.isBlank() || uf.isBlank()) {
                        JOptionPane.showMessageDialog(null, "Usuário NÂO cadastrado!");
                        break;
                    }

                    userDao.cadastrarUsuarioCompleto(u, rua, bairro, cidade, uf);
                    JOptionPane.showMessageDialog(null, "Usuário cadastrado com sucesso!");
                }

                // 8. Backup // simulação
                else if (op.startsWith("8")) {
                    try {
                        String cmd = "mysqldump -u root -p1234 libritech -r backup.sql";
                        JOptionPane.showMessageDialog(null, "Executando: " + cmd + "\n(Verifique a pasta do projeto)");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Simulação de Backup concluída.");
                    }

                }

                // 9. Listar livro Backup // simulação
                else if (op.startsWith("9")) {
                    try {
                        DefaultTableModel model = bookDao.listarAcervoTabela();
                        JTable tabela = new JTable(model);
                        tabela.setFillsViewportHeight(true);
                        JScrollPane scroll = new JScrollPane(tabela);
                        scroll.setPreferredSize(new Dimension(500, 300));

                        JOptionPane.showMessageDialog(null, scroll, "Acervo Disponível", JOptionPane.PLAIN_MESSAGE);
                    } catch (Exception ex) {
                        System.out.println("nao achei nada");
                    }
                }

            } catch (OperacaoCanceladaException e) {
                JOptionPane.showMessageDialog(null, "Operação cancelada.", "Aviso", JOptionPane.WARNING_MESSAGE);

            } catch (SQLException e) {
                if (e.getMessage().toLowerCase().contains("denied") || e.getErrorCode() == 1142) {
                    JOptionPane.showMessageDialog(null,
                            "ERRO: ACESSO NEGADO!\nSeu perfil não tem permissão.",
                            "Segurança", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Erro SQL: " + e.getMessage());
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Erro: Você deveria digitar um número!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
            }
        }
    }

    //  Se for null (cancelar), lança erro pra sair do fluxo.
    private static String pedir(String mensagem) {
        String valor = JOptionPane.showInputDialog(mensagem);
        if (valor == null) {
            throw new OperacaoCanceladaException(); // Aborta tudo
        }
        return valor;
    }
}