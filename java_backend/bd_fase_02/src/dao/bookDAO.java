package dao;

import model.entities.Book;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class bookDAO {

    private Connection conexao;

    public bookDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public void cadastrarBook(Book book) throws SQLException {
        String sql = "INSERT INTO livros (titulo, autor, isbn, preco_custo, quantidade_estoque) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setDouble(4, book.getPrice());
            ps.setInt(5,book.getInventory());

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

    public void emprestarBook(int idUsuario, int idLivro) throws SQLException {
        String sql = "CALL sp_transacao_emprestimo(?, ?)";

        try (PreparedStatement ps = conexao.prepareStatement(sql)){
            ps.setInt(1, idUsuario);
            ps.setInt(2, idLivro);

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

    // Consultar acervo - Aluno vw_acervo_publico
    public String listarAcervo() throws SQLException {
        String sql = "SELECT * FROM vw_acervo_publico";
        StringBuilder resultado = new StringBuilder();

        try (PreparedStatement ps = conexao.prepareStatement(sql)){
            java.sql.ResultSet rs = ps.executeQuery();{
                while (rs.next()) {
                    //faz um loop construtor da frase, isso vai deixar a parada um pitel, tem formataçaõ
                    resultado.append("Livro: ").append(rs.getString("Titulo")).append("\n");
                    resultado.append("Autor: ").append(rs.getString("Autor")).append("\n");
                    resultado.append("Status: ").append(rs.getString("Disponibilidade")).append("\n");
                    resultado.append("----------------------------\n");
                }
            }
        }
        return resultado.toString();
    }

    // para devolução
    public void devolverBook(int idEmprestimo) throws SQLException {
        String sql = "CALL sp_transacao_devolucao(?)";

        try (PreparedStatement ps = conexao.prepareStatement(sql)){
            ps.setInt(1, idEmprestimo);

            ps.execute();
        }
    }

    //Dash
    public String verDashboardFinanceiro() throws SQLException {
        String sql = "SELECT * FROM vw_dashboard_financeiro";
        StringBuilder texto = new StringBuilder();

        try (PreparedStatement ps = conexao.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()){
            if (rs.next()){
                texto.append("Multas pagas: ").append(rs.getInt("total_multas_pagas")).append("\n");
                texto.append("Arrecadação Total: R$ ").append(rs.getDouble("arrecadacao_total")).append("\n");

            }
        }
        return texto.toString();
    }

    // rank
    public String verRankingLeitura() throws SQLException {
        String sql = "SELECT * FROM vw_ranking_leitura"; //lembrar, limite está dentro da lógica do sheme.
        StringBuilder texto = new StringBuilder();

        try (PreparedStatement ps = conexao.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()){
            int pos = 1;
            while (rs.next()){
                texto.append(pos).append(" - ").append(rs.getString("titulo")).append("\n");
                pos++;
            }
        }
        return texto.toString();
    }

    //histórico
    public String verMeusEmprestimos(int idAluno) throws SQLException {
        String sql = "SELECT l.titulo, e.data_saida, e.data_previsao" +
                "FROM emprestimos" +
                "JOIN livros l ON e.id_livro_fk = l.id_livro" +
                "WHERE e.id_aluno_fk = ?";

        StringBuilder texto = new StringBuilder("==MEU EMPRESTIMOS==\n");
        try (PreparedStatement ps = conexao.prepareStatement(sql)){
            ps.setInt(1, idAluno);
            try (java.sql.ResultSet rs = ps.executeQuery()){
                if (!rs.next()){ //se vázio
                    return "Nenhum empréstimo encontrado para esse ID";
                }
                while (rs.next()){
                    texto.append("Livro: ").append(rs.getString("titulo")).append("\n");
                    texto.append("Pegou em: ").append(rs.getString("data_saida")).append("\n");
                    texto.append("Devolver até: ").append(rs.getString("data_previsao")).append("\n");
                    texto.append("------------------------------\n");
                }
            }
        }
        return texto.toString();
    }




}
