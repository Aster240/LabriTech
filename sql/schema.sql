DROP DATABASE IF EXISTS libritech;
CREATE DATABASE libritech;
USE libritech;

/* 1. TABELAS
======================================================= */

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    tipo ENUM('ALUNO', 'GERENTE', 'BIBLIOTECARIO', 'ESTAGIARIO') NOT NULL
);

CREATE TABLE enderecos (
    id_endereco INT AUTO_INCREMENT PRIMARY KEY,
    logradouro VARCHAR(150) NOT NULL,
    bairro VARCHAR(50) NOT NULL,
    cidade VARCHAR(50) NOT NULL,
    uf CHAR(2) NOT NULL,
    id_usuario_fk INT NOT NULL,
    CONSTRAINT fk_endereco_usuario FOREIGN KEY (id_usuario_fk) REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE
);

CREATE TABLE livros (
    id_livro INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    isbn VARCHAR(13) UNIQUE NOT NULL,
    preco_custo DECIMAL(10, 2) NOT NULL,
    quantidade_estoque INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL'
);

CREATE TABLE emprestimos (
    id_emprestimo INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario_fk INT NOT NULL,
    id_livro_fk INT NOT NULL,
    data_saida DATETIME DEFAULT CURRENT_TIMESTAMP,
    data_prevista DATE NOT NULL,
    data_devolucao DATETIME DEFAULT NULL,
    CONSTRAINT fk_emprestimo_usuario FOREIGN KEY (id_usuario_fk) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_emprestimo_livro FOREIGN KEY (id_livro_fk) REFERENCES livros(id_livro)
);

CREATE TABLE multas(
    id_multa INT AUTO_INCREMENT PRIMARY KEY,
    id_emprestimo_fk INT NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    pago BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_multa_emprestimo FOREIGN KEY (id_emprestimo_fk) REFERENCES emprestimos(id_emprestimo)
);

CREATE TABLE log_auditoria (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    tabela_afetada VARCHAR(50) NOT NULL,
    acao VARCHAR(20) NOT NULL,
    usuario_responsavel VARCHAR(100) NOT NULL, -- Corrigido o typo 'resposanvel'
    dados_antigos TEXT,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices de Performance
CREATE INDEX idx_usuario_email ON usuarios(email);
CREATE INDEX idx_livro_titulo ON livros(titulo);
CREATE INDEX idx_livro_isbn ON livros(isbn);

/*2. VIEWS (Segurança e Relatórios)
======================================================= */

-- View pública (esconde preço de custo)
CREATE OR REPLACE VIEW vw_acervo_publico AS
SELECT
    titulo AS 'Titulo',
    autor AS 'Autor',
    status AS 'Status Disponibilidade',
    CASE
        WHEN quantidade_estoque > 0 THEN 'Disponivel'
        ELSE 'Esgotado'
    END AS 'Disponibilidade'
FROM livros;

-- Ranking de Leitura
CREATE OR REPLACE VIEW vw_ranking_leitura AS
SELECT
    l.titulo,
    COUNT(e.id_emprestimo) AS 'total_emprestimos'
FROM livros l
JOIN emprestimos e ON l.id_livro = e.id_livro_fk
GROUP BY l.titulo
ORDER BY total_emprestimos DESC
LIMIT 10;

-- Dashboard Financeiro (Só Gerente vê)
CREATE OR REPLACE VIEW vw_dashboard_financeiro AS
SELECT
    COUNT(id_multa) AS total_multas_pagas,
    COALESCE(SUM(valor), 0) AS arrecadacao_total
FROM multas
WHERE pago = 1;

/* 3. STORED PROCEDURES (Regras de Negócio)
======================================================= */
DELIMITER $$

-- 3.1 Cadastro Completo
CREATE PROCEDURE sp_transacao_cadastro_usuario (
    IN p_name VARCHAR(100), IN p_cpf VARCHAR(11), IN p_email VARCHAR(100),
    IN p_senha VARCHAR(255), IN p_tipo VARCHAR(20),
    IN p_logradouro VARCHAR(150), IN p_bairro VARCHAR(50),
    IN p_cidade VARCHAR(50), IN p_uf CHAR(2)
)
BEGIN
    DECLARE v_novo_id_usuario INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
        INSERT INTO usuarios (nome,cpf,email,senha,tipo)
        VALUES (p_name, p_cpf, p_email, p_senha, p_tipo);

        SET v_novo_id_usuario = LAST_INSERT_ID();

        INSERT INTO enderecos (logradouro, bairro, cidade, uf, id_usuario_fk)
        VALUES (p_logradouro, p_bairro, p_cidade, p_uf, v_novo_id_usuario);
    COMMIT;
END$$

-- 3.2 Empréstimo
CREATE PROCEDURE sp_transacao_emprestimo (
    IN p_id_usuario INT,
    IN p_id_livro INT,
    IN p_dias_prazo INT
)
BEGIN
    DECLARE v_estoque INT;
    DECLARE v_pendencias INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
        SELECT quantidade_estoque INTO v_estoque FROM livros WHERE id_livro = p_id_livro FOR UPDATE;

        IF v_estoque <= 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: O livro nao esta disponivel em estoque.";
        END IF;

        -- Verifica se tem multas pendentes
        SELECT COUNT(*) INTO v_pendencias
        FROM multas m JOIN emprestimos e ON m.id_emprestimo_fk = e.id_emprestimo
        WHERE e.id_usuario_fk = p_id_usuario AND m.pago = 0;

        IF v_pendencias > 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: Usuario possui multas pendentes!";
        END IF;

        -- esperar o paramêtro do java, FALTAVA ISSO :/ (7 ou 14 dias) //
        INSERT INTO emprestimos (id_usuario_fk, id_livro_fk, data_prevista)
        VALUES (p_id_usuario, p_id_livro, DATE_ADD(CURRENT_DATE, INTERVAL p_dias_prazo DAY));

        -- Baixa no estoque
        UPDATE livros SET quantidade_estoque = quantidade_estoque - 1 WHERE id_livro = p_id_livro;
    COMMIT;
END$$

-- 3.3 Devolução (não tinha nos 4 obrigatórios tlgd zé? Porém tinha na página 6 e o quarto treco lá)
CREATE PROCEDURE sp_transacao_devolucao(IN p_id_emprestimo INT)
BEGIN
    DECLARE v_id_livro INT;
    DECLARE v_data_prevista DATE;
    DECLARE v_atraso INT;

    SELECT id_livro_fk, data_prevista INTO v_id_livro, v_data_prevista
    FROM emprestimos WHERE id_emprestimo = p_id_emprestimo;

    START TRANSACTION;
        -- Registra devolução
        UPDATE emprestimos SET data_devolucao = NOW() WHERE id_emprestimo = p_id_emprestimo;
        -- Sobe estoque
        UPDATE livros SET quantidade_estoque = quantidade_estoque + 1 WHERE id_livro = v_id_livro;

        -- Calcula multa se houver atraso (R$ 2.00 por dia)
        SET v_atraso = DATEDIFF(CURDATE(), v_data_prevista);
        IF v_atraso > 0 THEN
            INSERT INTO multas (id_emprestimo_fk, valor, pago) VALUES (p_id_emprestimo, v_atraso * 2.00, 0);
        END IF;
    COMMIT;
END$$

-- 3.4 Renovar
CREATE PROCEDURE sp_renovar_emprestimo (IN p_id_emprestimo INT)
BEGIN
    DECLARE v_status_livro VARCHAR(20);

    -- Busca status do livro relacionado ao emprestimo
    SELECT l.status INTO v_status_livro
    FROM emprestimos e JOIN livros l ON e.id_livro_fk = l.id_livro
    WHERE e.id_emprestimo = p_id_emprestimo;

    IF v_status_livro = 'RESERVADO' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: Livro reservado ou indisponivel.";
    ELSE
        UPDATE emprestimos SET data_prevista = DATE_ADD(data_prevista, INTERVAL 7 DAY)
        WHERE id_emprestimo = p_id_emprestimo;
    END IF;
END$$

-- 3.5 Histórico MAIS seguro, o aluno não acessa ela diretamente!
CREATE PROCEDURE sp_historico_usuario(IN p_id_aluno INT)
BEGIN
    SELECT l.titulo, e.data_saida, e.data_prevista, e.data_devolucao
    FROM emprestimos e
    JOIN livros l ON e.id_livro_fk = l.id_livro
    WHERE e.id_usuario_fk = p_id_aluno;
END$$

DELIMITER ;

/* 4. TRIGGERS
======================================================= */
DELIMITER $$

-- Horário Comercial
CREATE TRIGGER trg_trava_horario_comercial
BEFORE INSERT ON emprestimos
FOR EACH ROW
BEGIN
    -- Bloqueia antes das 08h ou depois das 18h // Vou tirar para teste isso em uma query depois.
    IF HOUR(CURRENT_TIME()) < 8 OR HOUR(CURRENT_TIME()) >= 18 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: Operacoes permitidas apenas em horario comercial (8h-18h).";
    END IF;
END$$

-- Auditoria de Exclusão
CREATE TRIGGER trg_auditoria_delecao
AFTER DELETE ON livros
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela_afetada, acao, usuario_responsavel, dados_antigos)
    VALUES ('livros', 'DELETE', USER(), CONCAT('ID: ', OLD.id_livro, ', Titulo: ', OLD.titulo, ', ISBN: ', OLD.isbn));
END$$

-- Limite de 3 livros
CREATE TRIGGER trg_limite_emprestimos
BEFORE INSERT ON emprestimos
FOR EACH ROW
BEGIN
    DECLARE v_qtd_livros INT;
    SELECT COUNT(*) INTO v_qtd_livros FROM emprestimos
    WHERE id_usuario_fk = NEW.id_usuario_fk AND data_devolucao IS NULL;

    IF v_qtd_livros >= 3 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: Limite de 3 livros emprestados atingido.";
    END IF;
END$$

-- Estoque não negativo
CREATE TRIGGER trg_preventiva_estoque
BEFORE UPDATE ON livros
FOR EACH ROW
BEGIN
    IF NEW.quantidade_estoque < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: A quantidade em estoque nao pode ser negativa.";
    END IF;
END$$

DELIMITER ;

/* 5. USUÁRIOS E PERMISSÕES
======================================================= */
DROP USER IF EXISTS 'usr_gerente'@'localhost';
DROP USER IF EXISTS 'usr_bibliotecario'@'localhost';
DROP USER IF EXISTS 'usr_estagiario'@'localhost';
DROP USER IF EXISTS 'usr_aluno'@'localhost';

-- Gerente
CREATE USER 'usr_gerente'@'localhost' IDENTIFIED BY 'senha_foda';
GRANT ALL PRIVILEGES ON libritech.* TO 'usr_gerente'@'localhost';

-- Bibliotecário
CREATE USER 'usr_bibliotecario'@'localhost' IDENTIFIED BY 'senha_chata';
GRANT SELECT, INSERT, UPDATE ON libritech.livros TO 'usr_bibliotecario'@'localhost';
GRANT SELECT, INSERT, UPDATE ON libritech.usuarios TO 'usr_bibliotecario'@'localhost';
GRANT SELECT, INSERT, UPDATE ON libritech.emprestimos TO 'usr_bibliotecario'@'localhost';
GRANT SELECT ON libritech.vw_acervo_publico TO 'usr_bibliotecario'@'localhost';
GRANT SELECT ON libritech.vw_ranking_leitura TO 'usr_bibliotecario'@'localhost';
GRANT SELECT ON libritech.enderecos TO 'usr_bibliotecario'@'localhost';
GRANT SELECT, INSERT ON libritech.log_auditoria TO 'usr_bibliotecario'@'localhost';
-- Permissão para rodar as procedures
GRANT EXECUTE ON PROCEDURE libritech.sp_transacao_emprestimo TO 'usr_bibliotecario'@'localhost';
GRANT EXECUTE ON PROCEDURE libritech.sp_renovar_emprestimo TO 'usr_bibliotecario'@'localhost';
GRANT EXECUTE ON PROCEDURE libritech.sp_transacao_devolucao TO 'usr_bibliotecario'@'localhost';
GRANT EXECUTE ON PROCEDURE libritech.sp_transacao_cadastro_usuario TO 'usr_bibliotecario'@'localhost';

-- Estagiário Não pode deletar e não vê financeiro, falta privilégios no coitchado
CREATE USER 'usr_estagiario'@'localhost' IDENTIFIED BY 'senha_fraca';
GRANT SELECT ON libritech.livros TO 'usr_estagiario'@'localhost';
GRANT SELECT ON libritech.usuarios TO 'usr_estagiario'@'localhost';
GRANT INSERT ON libritech.emprestimos TO 'usr_estagiario'@'localhost';
GRANT SELECT ON libritech.vw_acervo_publico TO 'usr_estagiario'@'localhost';
GRANT EXECUTE ON PROCEDURE libritech.sp_transacao_emprestimo TO 'usr_estagiario'@'localhost';
REVOKE DELETE ON libritech.livros FROM 'usr_estagiario'@'localhost';

-- Aluno (Acesso restrito)
CREATE USER 'usr_aluno'@'localhost' IDENTIFIED BY 'senha_idiota';
GRANT SELECT ON libritech.vw_acervo_publico TO 'usr_aluno'@'localhost';
GRANT SELECT ON libritech.vw_ranking_leitura TO 'usr_aluno'@'localhost';
GRANT EXECUTE ON PROCEDURE libritech.sp_historico_usuario TO 'usr_aluno'@'localhost';

FLUSH PRIVILEGES;