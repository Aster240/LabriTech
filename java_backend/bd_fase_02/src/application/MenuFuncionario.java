package application;

import dao.BookDAO;
import dao.UserDAO;
import dao.AuditDAO; // <--- 1. IMPORT NOVO
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

    private static class OperacaoCanceladaException extends RuntimeException {}

    public static void iniciar(Connection conn) {

        BookDAO bookDao = new BookDAO(conn);
        UserDAO userDao = new UserDAO(conn);
        AuditDAO auditDao = new AuditDAO(conn); // <--- 2. INSTANCIA NOVA

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
                "10. Ver Logs de Auditoria",
                "11. Ver Livros Emprestados",
                "Voltar"
        };

        while(true) {
            String op = (String) JOptionPane.showInputDialog(null, "Painel Administrativo", "Menu Funcionário",
                    JOptionPane.PLAIN_MESSAGE, null, actions, actions[0]);

            if (op == null || op.equals("Voltar")) break;

            try {
                // 1. Cadastrar Livro
                if (op.startsWith("1.")) {
                    String titulo = pedir("Título do Livro:");
                    String autor = pedir("Autor:");
                    String isbn = pedir("ISBN:");
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
                    String[] tipos = {"Aluno (7 dias)", "Funcionário (14 dias)"};
                    int escolha = JOptionPane.showOptionDialog(null, "Quem está pegando o livro?", "Regra de Prazo",
                            0, 3, null, tipos, tipos[0]);
                    if (escolha == -1) throw new OperacaoCanceladaException();

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
                    User u = (regraSelecionada == Rule.ALUNO) ? new Student() : new Employee();

                    u.setRule(regraSelecionada);
                    u.setName(pedir("Nome Completo:"));
                    u.setCpf(pedir("CPF:"));
                    u.setEmail(pedir("Email:"));
                    u.setPassword(pedir("Senha:"));

                    String rua = pedir("Rua:");
                    String bairro = pedir("Bairro:");
                    String cidade = pedir("Cidade:");
                    String uf = pedir("UF (2 letras):");

                    if (rua.isBlank() || bairro.isBlank()) {
                        JOptionPane.showMessageDialog(null, "Dados incompletos!");
                        break;
                    }

                    userDao.cadastrarUsuarioCompleto(u, rua, bairro, cidade, uf);
                    JOptionPane.showMessageDialog(null, "Usuário cadastrado com sucesso!");
                }

                // 8. Backup
                else if (op.startsWith("8")) {
                    try {
                        String cmd = "mysqldump -u root -p1234 libritech -r backup.sql";
                        JOptionPane.showMessageDialog(null, "Simulação: " + cmd);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Backup simulado.");
                    }
                }

                // 9. Listar Livros
                else if (op.startsWith("9")) {
                    DefaultTableModel model = bookDao.listarAcervoTabela();
                    JTable tabela = new JTable(model);
                    tabela.setFillsViewportHeight(true);
                    JScrollPane scroll = new JScrollPane(tabela);
                    scroll.setPreferredSize(new Dimension(600, 300));
                    JOptionPane.showMessageDialog(null, scroll, "Acervo", JOptionPane.PLAIN_MESSAGE);
                }

                // 10. ver logs
                else if (op.startsWith("10")) {
                    DefaultTableModel model = auditDao.listarLogs();
                    JTable tabela = new JTable(model);


                    JScrollPane scroll = new JScrollPane(tabela);
                    scroll.setPreferredSize(new Dimension(900, 300));

                    JOptionPane.showMessageDialog(null, scroll, "Logs de Auditoria", JOptionPane.PLAIN_MESSAGE);
                }

                else if (op.startsWith("11.")) {
                    DefaultTableModel model = bookDao.listarEmprestimosAtivos();
                    JTable tabela = new JTable(model);


                    tabela.getColumnModel().getColumn(1).setPreferredWidth(200);
                    tabela.getColumnModel().getColumn(2).setPreferredWidth(150);

                    JScrollPane scroll = new JScrollPane(tabela);
                    scroll.setPreferredSize(new Dimension(800, 300));

                    JOptionPane.showMessageDialog(null, scroll, "Livros atualmente emprestados", JOptionPane.PLAIN_MESSAGE);
                }

            } catch (OperacaoCanceladaException e) {
                JOptionPane.showMessageDialog(null, "Operação cancelada.", "Aviso", JOptionPane.WARNING_MESSAGE);

            } catch (SQLException e) {
                if (e.getMessage().toLowerCase().contains("denied") || e.getErrorCode() == 1142) {
                    JOptionPane.showMessageDialog(null,
                            "ERRO: ACESSO NEGADO!\nSeu perfil (" + getUsuarioAtual(conn) + ") não tem permissão.",
                            "Segurança", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Erro SQL: " + e.getMessage());
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Erro: Digite apenas números nos campos de ID/Quantidade!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
            }
        }
    }

    private static String pedir(String mensagem) {
        String valor = JOptionPane.showInputDialog(mensagem);
        if (valor == null) throw new OperacaoCanceladaException();
        return valor;
    }


    private static String getUsuarioAtual(Connection conn) {
        try {
            return conn.getMetaData().getUserName();
        } catch (SQLException e) {
            return "Desconhecido";
        }
    }
}