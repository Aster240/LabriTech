package dao;

import model.entities.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class userDAO {

    private Connection conexao;

    public userDAO(Connection conexao) {
        this.conexao = conexao;
    }


    public void cadastrarUsuarioCompleto(User user, String logradouro, String bairro, String cidade, String uf) throws SQLException {
        String sql = "CALL sp_transacao_cadastro_usuario(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            // Dados do Usuário (Objeto User)
            ps.setString(1, user.getName());
            ps.setString(2, user.getCpf());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());

            // precisou converter de ENUM para String // opções
            ps.setString(5, user.getRule().toString());

            ps.setString(6, logradouro);
            ps.setString(7, bairro);
            ps.setString(8, cidade);
            ps.setString(9, uf);

            ps.execute();
        }
    }
}