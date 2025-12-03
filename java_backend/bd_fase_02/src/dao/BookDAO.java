package dao;

import model.entities.Book;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class BookDAO {

    private Connection conexao;

    public BookDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public void cadastrarBook(Book book) throws SQLException {
        String sql = "INSERT INTO livros (titulo, autor, isbn, preco_custo, quantidade_estoque) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setDouble(4, book.getPrice());
            ps.setInt(5, book.getInventory());
            ps.execute();
        }
    }

    public void excluirBook(int id) throws SQLException {
        String sql = "DELETE FROM livros WHERE id_livro = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.execute();
        }
    }

    public void emprestarBook(int idUsuario, int idLivro, int diasPrazo) throws SQLException {
        String sql = "CALL sp_transacao_emprestimo(?, ?, ?)"; // 3 interrogações
        try (PreparedStatement ps = conexao.prepareStatement(sql)){
            ps.setInt(1, idUsuario);
            ps.setInt(2, idLivro);
            ps.setInt(3, diasPrazo); // Passa o prazo para o banco
            ps.execute();
        }
    }

    public void renovar(int idEmprestimo) throws SQLException {
        String sql = "CALL sp_renovar_emprestimo(?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql)){
            ps.setInt(1, idEmprestimo);
            ps.execute();
        }
    }

    public void devolverBook(int idEmprestimo) throws SQLException {
        String sql = "CALL sp_transacao_devolucao(?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql)){
            ps.setInt(1, idEmprestimo);
            ps.execute();
        }
    }

    //  List acervo em forma de tabela!
    public DefaultTableModel listarAcervoTabela() throws SQLException {
        String sql = "SELECT * FROM vw_acervo_publico";
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("Título");
        modelo.addColumn("Autor");
        modelo.addColumn("Situação");
        modelo.addColumn("Disponibilidade");

        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getString("Titulo"),
                        rs.getString("Autor"),
                        rs.getString("Status Disponibilidade"),
                        rs.getString("Disponibilidade")
                });
            }
        }
        return modelo;
    }

    // Histórico Seguro (Procedure)
    public String verMeusEmprestimos(int idAluno) throws SQLException {
        String sql = "{CALL sp_historico_usuario(?)}";
        StringBuilder texto = new StringBuilder("== HISTÓRICO ==\n\n");

        try (CallableStatement cs = conexao.prepareCall(sql)) {
            cs.setInt(1, idAluno);
            try (ResultSet rs = cs.executeQuery()) {
                if (!rs.isBeforeFirst()) return "Nenhum registro encontrado.";
                while (rs.next()) {
                    texto.append("Livro: ").append(rs.getString("titulo")).append("\n");
                    texto.append("Saída: ").append(rs.getTimestamp("data_saida")).append("\n");
                    texto.append("Vence: ").append(rs.getDate("data_prevista")).append("\n");

                    if (rs.getObject("data_devolucao") != null)
                        texto.append("Devolvido em: ").append(rs.getTimestamp("data_devolucao")).append("\n");
                    else
                        texto.append("STATUS: PENDENTE\n");

                    texto.append("-----------------\n");
                }
            }
        }
        return texto.toString();
    }

    // Dashboards
    public String verDashboardFinanceiro() throws SQLException {
        String sql = "SELECT * FROM vw_dashboard_financeiro";
        try (Statement st = conexao.createStatement(); ResultSet rs = st.executeQuery(sql)){
            if (rs.next()) return "Multas Pagas: " + rs.getInt("total_multas_pagas") + "\nTotal R$: " + rs.getDouble("arrecadacao_total");
        }
        return "Sem dados";
    }

    public String verRankingLeitura() throws SQLException {
        String sql = "SELECT * FROM vw_ranking_leitura";
        StringBuilder sb = new StringBuilder();
        try (Statement st = conexao.createStatement(); ResultSet rs = st.executeQuery(sql)){
            int i = 1;
            while(rs.next()) sb.append(i++).append(". ").append(rs.getString("titulo")).append("\n");
        }
        return sb.toString();
    }
}