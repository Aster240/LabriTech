package dao;

import model.entities.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDAO { // Nome da classe ajustado para PascalCase

    private Connection conexao;

    public UserDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public void cadastrarUsuarioCompleto(User user, String logradouro, String bairro, String cidade, String uf) throws SQLException {

        String sql = "CALL sp_transacao_cadastro_usuario(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            // Dados comuns a todos (herdados de User)
            ps.setString(1, user.getName());
            ps.setString(2, user.getCpf());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());

            // O Enum Rule converte para a String que o banco espera ('ALUNO', 'GERENTE', etc)
            ps.setString(5, user.getRule().toString());

            // Dados de endereço
            ps.setString(6, logradouro);
            ps.setString(7, bairro);
            ps.setString(8, cidade);
            ps.setString(9, uf);

            ps.execute();
        }
    }
}