package application;

import db.Conn;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        // Opções iniciais
        String[] options = {"Funcionário", "Aluno", "Sair"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Qual é o seu perfil de acesso?",
                "Sistema LibriTech",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 2 || choice == -1) {
            System.exit(0);
        }

        // Login no banco de dados (pede usuario e senha do MySQL)
        String dbUser = JOptionPane.showInputDialog("Usuário do Banco:");
        if (dbUser == null) System.exit(0);

        String dbPass = JOptionPane.showInputDialog("Senha do Banco:");
        if (dbPass == null) System.exit(0);

        Connection conn = null;

        try {
            // Tenta conectar com o usuario digitado
            conn = Conn.loginToDB(dbUser, dbPass);
            JOptionPane.showMessageDialog(null, "Logado como: " + dbUser);

            // Redireciona para o menu correto
            if (choice == 0) {
                menuFuncionario(conn);
            } else {
                menuAluno(conn);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro de login: " + e.getMessage());
        } finally {
            Conn.closeConnection();
        }
    }

    private static void menuFuncionario(Connection conn) {
        String[] actions = {
                "1. Cadastrar Livro",
                "2. Realizar Empréstimo",
                "3. Renovar Empréstimo",
                "4. Realizar Devolução",
                "5. Excluir Livro",
                "6. Relatórios",
                "7. Sair"
        };

        while (true) {
            String selected = (String) JOptionPane.showInputDialog(
                    null,
                    "Escolha uma operação:",
                    "Menu Funcionário",
                    JOptionPane.PLAIN_MESSAGE,
                    null, actions, actions[0]);

            if (selected == null || selected.equals("7. Sair")) break;

            if (selected.startsWith("1")) {
                cadastrarLivro(conn);
            }
            else if (selected.startsWith("2")) {
                realizarEmprestimo(conn);
            }
            else if (selected.startsWith("3")) {
                realizarDevolucao(conn);
            }
            else if (selected.startsWith("4")) {
                realizarDevolucao(conn);
            }
            else if (selected.startsWith("5")) {
                excluirLivro(conn); // Chama o teste de segurança
            }
            else if (selected.startsWith("6")) {
                JOptionPane.showMessageDialog(null, "Em desenvolvimento...");
            }
        }
    }

    private static void menuAluno(Connection conn) {
        String[] actions = {
                "1. Consultar Acervo",
                "2. Meus Empréstimos",
                "3. Sair"
        };

        while (true) {
            String selected = (String) JOptionPane.showInputDialog(
                    null,
                    "Área do Aluno:",
                    "Menu Aluno",
                    JOptionPane.PLAIN_MESSAGE,
                    null, actions, actions[0]);

            if (selected == null || selected.equals("3. Sair")) break;

            if (selected.startsWith("1")) {
                consultarAcervo(conn);
            }
            else if (selected.startsWith("2")) {
                JOptionPane.showMessageDialog(null, "Em desenvolvimento...");
            }
        }
    }

    // Tenta excluir um livro para testar as permissoes do banco
    private static void excluirLivro(Connection conn) {
        String idStr = JOptionPane.showInputDialog("ID do Livro para Excluir:");
        if (idStr == null) return;

        try {
            int id = Integer.parseInt(idStr);
            String sql = "DELETE FROM livros WHERE id_livro = ?";
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, id);
            int rows = st.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(null, "Livro excluído com sucesso.");
            } else {
                JOptionPane.showMessageDialog(null, "Livro não encontrado.");
            }
            st.close();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID Inválido.");
        } catch (SQLException e) {
            // Captura o erro de permissão do MySQL (caso seja estagiário)
            JOptionPane.showMessageDialog(null,
                    "Acesso Negado: Seu usuário não tem permissão para excluir.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    //  CONSULTAR ACERVO (Para Alunos)
    private static void consultarAcervo(Connection conn) {
        try {
            // Busca os dados da View publica conforme o PDF pede
            String sql = "SELECT * FROM vw_acervo_publico";
            PreparedStatement st = conn.prepareStatement(sql);
            java.sql.ResultSet rs = st.executeQuery();

            String texto = "";
            while (rs.next()) {
                // Monta o texto linha por linha de forma simples
                texto += "Livro: " + rs.getString("Titulo") + " | " +
                        "Autor: " + rs.getString("Autor") + " | " +
                        "Disp: " + rs.getString("Disponibilidade") + "\n";
            }

            // Usa um TextArea para garantir que dê para rolar a tela se tiver muitos livros
            javax.swing.JTextArea textArea = new javax.swing.JTextArea(texto);
            textArea.setEditable(false); // Bloqueia edição

            // Joga o TextArea dentro de uma janela com Scroll
            javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(textArea);
            scroll.setPreferredSize(new java.awt.Dimension(400, 300));

            JOptionPane.showMessageDialog(null, scroll, "Acervo", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar: " + e.getMessage());
        }
    }

    //  REALIZAR EMPRÉSTIMO (Para Funcionários)
    private static void realizarEmprestimo(Connection conn) {
        try {
            String alunoStr = JOptionPane.showInputDialog("ID do Aluno:");
            String livroStr = JOptionPane.showInputDialog("ID do Livro:");

            if (alunoStr == null || livroStr == null) return;

            int idAluno = Integer.parseInt(alunoStr);
            int idLivro = Integer.parseInt(livroStr);

            String sql = "CALL sp_transacao_emprestimo(?, ?)";
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, idAluno);
            st.setInt(2, idLivro);
            st.execute();

            JOptionPane.showMessageDialog(null, "Empréstimo realizado com sucesso!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
    }

    // REALIZAR DEVOLUÇÃO (Para Funcionários)
    private static void realizarDevolucao(Connection conn) {
        try {
            String empStr = JOptionPane.showInputDialog("ID do Empréstimo:");
            if (empStr == null) return;

            int idEmprestimo = Integer.parseInt(empStr);

            // Chama a procedure de devolução que calcula multa e repõe estoque
            String sql = "CALL sp_transacao_devolucao(?)";
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, idEmprestimo);
            st.execute();

            JOptionPane.showMessageDialog(null, "Livro devolvido!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro na devolução: " + e.getMessage());
        }
    }

    private static void renovarEmprestimo(Connection conn) {
        try {
            String empStr = JOptionPane.showInputDialog("ID do Emprestimo para renovar: ");
            if (empStr == null) return;
            int idEmprestimo = Integer.parseInt(empStr);

            //não aguento mais :/
            String sql = "CALL sp_renovar_emprestimo(?)";
            PreparedStatement st =  conn.prepareStatement(sql);
            st.setInt(1, idEmprestimo);
            st.execute();

            JOptionPane.showMessageDialog(null, "Renovação realizada com sucesso!");

        }catch (Exception e){
            JOptionPane.showMessageDialog(null, "Erro ao renovar: " + e.getMessage());
        }
    }

    private static void cadastrarLivro(Connection conn) {
        try {
            // Pede os dados básicos do livro
            String titulo = JOptionPane.showInputDialog("Título do Livro:");
            String autor = JOptionPane.showInputDialog("Autor:");
            String isbn = JOptionPane.showInputDialog("ISBN (Código):");

            String precoStr = JOptionPane.showInputDialog("Preço de Custo (Ex: 50.00):");
            double preco = Double.parseDouble(precoStr.replace(",", ".")); // Troca virgula por ponto pra não dar erro

            String qtdStr = JOptionPane.showInputDialog("Quantidade em Estoque:");
            int qtd = Integer.parseInt(qtdStr);

            String sql = "INSERT INTO livros (titulo, autor, isbn, preco_custo, quantidade_estoque) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement st = conn.prepareStatement(sql);
            st.setString(1, titulo);
            st.setString(2, autor);
            st.setString(3, isbn);
            st.setDouble(4, preco);
            st.setInt(5, qtd);

            int rows = st.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(null, "Livro cadastrado com sucesso!");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Preço ou Quantidade inválidos (use números).");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao cadastrar: " + e.getMessage());
        }
    } //negativo :(

}