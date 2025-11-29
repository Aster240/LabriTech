CREATE DATABASE IF NOT EXISTS libritech;
USE libritech;

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    tipo ENUM('ALUNO', 'GERENTE', 'BIBLIOTECARIO', 'ESTAGIARIO') NOT NULL -- David deu as duas opções, botei ENUM pq achei mais pratico, ceis preferirem VARCHAR podem mudar dboa --
);

CREATE TABLE enderecos (
    id_endereco INT AUTO_INCREMENT PRIMARY KEY,
    logradouro VARCHAR(150) NOT NULL,
    bairro VARCHAR(50) NOT NULL,
    cidade VARCHAR(50) NOT NULL,
    uf CHAR(2) NOT NULL,
    id_usuario_fk INT NOT NULL,
    CONSTRAINT fk_endereco_usuario FOREIGN KEY (id_usuario_fk) REFERENCES usuarios(id_usuario_fk)
        ON DELETE CASCADE -- Se o id do usuario for deletado, tudo sobre o endereço dele vai junto, questão de segurança; O que acham? -- 
);

CREATE TABLE livros (
    id_livro INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    isbn VARCHAR(13) UNIQUE NOT NULL,
    preco_custo DECIMAL(10, 2) NOT NULL,
    quantidade_estoque INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL' -- Geralmente quando o livro chega ele sempre ta DISPONIVEL, achei interessante a função --
);

CREATE TABLE emprestimos (
    id_emprestimo INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario_fk INT NOT NULL,
    id_livro_fk INT NOT NULL,
    data_saida DATETIME DEFAULT CURRENT_TIMESTAMP, -- Preenche de automatico a data e hora --
    data_prevista DATE NOT NULL,
    data_devolucao DATETIME DEFAULT NULL
    CONSTRAINT fk_emprestimo_usuario FOREIGN KEY (id_usuario_fk) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_emprestimo_livro FOREIGN KEY (id_livro_fk) REFERENCES livros(id_livro)
);

CREATE TABLE multas(
    id_multa INT AUTO_INCREMENT PRIMARY KEY,
    id_emprestimo_fk INT NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    pago BOOLEAN DEFAULT FALSE, -- Botei por Default como falso porque se tem multa automaticamente é pq ainda não pagaram --
);

CREATE TABLE log_auditoria (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    tabela_afetada VARCHAR(50) NOT NULL,
    acao VARCHAR(20) NOT NULL,
    usuario_responsavel VARCHAR(100) NOT NULL,
    dados_antigos TEXT,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP; 
);

-- Os indices que David pediu -- 
CREATE INDEX idx_usuario_email ON usuarios(email);
CREATE INDEX idx_livro_titulo ON livros(titulo);
CREATE INDEX idx_livro_isbn ON livros(isbn);

-- Aqui ficam as questões como segurança e os privilegios dos user -- 

CREATE USER 'usr_gerente'@'localhost' IDENTIFIED BY 'senha_foda';
GRANT ALL PRIVILEGES ON libritech.* TO 'usr_gerente'@'localhost';

CREATE USER 'usr_bibliotecario'@'localhost' IDENTIFIED BY 'senha_chata';
GRANT SELECT, INSERT, UPDATE ON libritech.livros TO 'usr_bibliotecario'@'localhost';
GRANT SELECT, INSERT, UPDATE ON libritech.usuarios TO 'usr_bibliotecario'@'localhost';
GRANT SELECT, INSERT, UPDATE ON libritech.emprestimos TO 'usr_bibliotecario'@'localhost';
GRANT SELECT ON libritech.enderecos TO 'usr_bibliotecario'@'localhost';
GRANT SELECT, INSERT ON libritech.log_auditoria TO 'usr_bibliotecario'@'localhost';

CREATE USER 'usr_estagiario'@'localhost' IDENTIFIED BY 'senha_fraca';
GRANT SELECT ON libritech.livros TO 'usr_estagiario'@'localhost';
GRANT SELECT ON libritech.usuarios TO 'usr_estagiario'@'localhost';
GRANT INSERT ON libritech.emprestimos TO 'usr_estagiario'@'localhost';

CREATE USER 'usr_aluno'@'localhost' IDENTIFIED BY 'senha_idiota';
GRANT SELECT ON libritech.vw_acerto_publico TO 'usr_aluno'@'localhost'; -- So dar fazer após criação da view --
GRANT SELECT ON libritech.vw_ranking_leitura TO 'usr_aluno'@'localhost';

FLUSH PRIVILEGES;

-- stored procedures -- 
-- Emprestimo de livro --
DELIMITER $$

CREATE PROCEDURE sp_transacao_emprestimo (
    IN p_id_usuario INT,
    IN p_id_livro INT
)
BEGIN 
    DECLARE v_estoque INT,
    DECLARE v_pendencias INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

START TRANSACTION;
    SELECT quantidade_estoque INTO v_estoque 
    FROM livros 
    WHERE id_livro = p_id_livro FOR UPDATE;
    IF v_estoque <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: O livro nao esta disponivel em estoque.";
    END IF;
    SELECT COUNT(*) INTO v_pendencias 
    FROM multas m JOIN emprestimos e ON m.id_emprestimo_fk = e.id_emprestimo 
    WHERE e.id_usuario_fk = p_id_usuario AND m.pago = 0; -- 0 é FALSE; 1 é TRUE --
    IF v_pendencias > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: Usuario possui multas pendentes! LARAPIO!!";
    END IF;

    INSERT INTO emprestimos (id_usuario_fk, id_livro_fk, data_prevista )
    VALUES (p_id_usuario, p_id_livro, DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY)); -- Vai pegar a data atual e somar uma semana para a devolução --
    UPDATE livros SET quantidade_estoque = quantidade_estoque - 1
    WHERE id_livro = p_id_livro;

    COMMIT;
END$$

DELIMITER ;

-- Renovar de emprestimo -- 
DELIMITER $$

CREATE PROCEDURE sp_renovar_emprestimo (
    IN p_id_emprestimo INT
    )
    BEGIN
        DECLARE v_status_livro VARCHAR(20);
        DECLARE v_id_livro INT;
        SELECT id_livro_fk INTO v_id_livro
        FROM emprestimos
        WHERE id_emprestimo = p_id_emprestimo;
        IF v_status_livro = 'RESERVADO' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: Livro reservado ou indisponivel.";
        ELSE 
            UPDATE emprestimos
            SET data_prevista = DATE_ADD(data_prevista, INTERVAL 7 DAY)
            WHERE id_emprestimo = p_id_emprestimo;
        END IF;
END$$

DELIMITER ;

-- dale tarifaxo -- 
DELIMITER $$
    
    CREATE PROCEDURE sp_calcular_multa (
        IN p_id_emprestimo INT,
        OUT p_valor_multa DECIMAL(10,2)
)
BEGIN
    DECLARE v_data_prevista DATE;
    DECLARE v_dias_atraso INT;
    DECLARE v_taxa_diaria DECIMAL(10,2) DEFAULT 2.00; -- Valor fixo de multa diaria --

    SELECT data_prevista INTO v_data_prevista
    FROM emprestimos
    WHERE id_emprestimo = p_id_emprestimo;
    SET v_dias_atraso = DATEDIFF(CURRENT_DATE, v_data_prevista);
    IF v_dias_atraso > 0 THEN
        SET p_valor_multa = v_dias_atraso * v_taxa_diaria;
    ELSE
        SET p_valor_multa = 0.00;
    END IF;
END$$

DELIMITER ;

-- cadastros -- 
DELIMITER $$

CREATE PROCEDURE sp_transacao_cadastro_usuario (
    IN p_name VARCHAR(100),
    IN p_cpf VARCHAR(11),
    IN p_email VARCHAR(100),
    IN p_senha VARCHAR(255),
    IN p_tipo VARCHAR(20), -- ALUNO, GERENTE, BIBLIOTECARIO, ESTAGIARIO; Tecnicamente ele vai encaixar em um desses na hora do cadastro, mas ainda precisa definir --
    IN P_logradouro VARCHAR(150),
    IN P_bairro VARCHAR(50),
    IN P_cidade VARCHAR(50),
    IN P_uf CHAR(2)
)
BEGIN
    DECLARE v_nobo_id_usuario INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
START TRANSACTION;
    INSERT INTO usuarios (nome,cpf,email,senha,tipo)
    VALUES (p_name, p_cpf, p_email, p_senha, p_tipo);
    SET v_nobo_id_usuario = LAST_INSERT_ID();
    INSERT INTO enderecos (logradouro, bairro, cidade, uf, id_usuario_fk)
    VALUES (p_logradouro, p_bairro, p_cidade, p_uf, v_nobo_id_usuario);

    COMMIT;
END$$

DELIMITER ;

-- Triggers --
-- horario comercial --
DELIMITER $$
CREATE TRIGGER trg_trava_horario_comercial
BEFORE INSERT ON emprestimos
FOR EACH ROW
BEGIN
    IF HOUR(CURRENT_TIME()) <8 OR HOUR(CURRENT_TIME()) >=18 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: Operacoes permitidas apenas em horario comercial (8h-18h).";
    END IF;
END$$

DELIMITER ;

-- auditoria do DELETE em livros -- 
DELIMITER $$
CREATE TRIGGER trg_auditoria_delecao
AFTER DELETE ON livros
FOR EACH ROW
BEGIN
    INSERT INTO log_auditoria (tabela_afetada, acao, usuario_resposanvel, dados_antigos)
    VALUES ('livros', 'DELETE', USER(), CONCAT('ID: ', OLD.id_livro, ', Titulo: ', OLD.titulo, ', ISBN: ', OLD.isbn)
    );
END$$

DELIMITER ;

-- Limite de Emprestimos -- 
DELIMITER $$
CREATE TRIGGER trg_limite_emprestimos
BEFORE INSERT ON emprestimos
FOR EACH ROW
BEGIN
    DECLARE v_qtd_livros INT;
    SELECT COUNT(*) INTO v_qtd_livros
    FROM emprestimos
    WHERE id_usuario_fk = NEW.id_usuario_fk AND data_devolucao IS NULL;
    IF v_qtd_livros >=3 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: Limite de 3 livros emprestados atingido.";
    END IF;
END$$

-- Preventiva de Estoque --
CREATE TRIGGER trg_preventiva_estoque
BEFORE UPDATE ON livros
FOR EACH ROW
BEGIN
 IF NEW.quantidade_estoque<0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: A quantidade em estoque nao pode ser negativa.";
 END IF;
END$$

DELIMITER ;

-- Views --
-- View acervo publico --
CREATE VIEW vw_acervo_publico AS
SELECT 
    titulo AS 'Titulo',
    autor AS 'Autor',
    status AS 'Status Disponibilidade',
    CASE 
        WHEN quantidade_estoque > 0 THEN 'Disponivel' 
        ELSE 'Esgotado' 
    END AS 'Disponibilidade'
FROM livros;

-- View Livros Atrasados --
CREATE VIEW vw_livros_atrasados AS
SELECT
    e.id_emprestimo,
    u.nome AS 'Aluno',
    u.email AS 'Contato',
    l.titulo AS 'Livro',
    e.data_prevista AS 'Vencimento',
    DATEDIFF(CURRENT_DATE, e.data_prevista) AS 'Dias Atraso'
FROM emprestimos e
JOIN usuarios u ON e.id_usuario_fk = u.id_usuario
JOIN livros l ON e.id_livro_fk = l.id_livro
WHERE e.data_devolucao IS NULL AND e.data_prevista < CURRENT_DATE;

-- View Ranking de Leitura --
CREATE VIEW vw_ranking_leitura AS
SELECT
    l.titulo,
    COUNT(e.id_emprestimo) AS 'Total Emprestimos'
FROM livros l
JOIN emprestimos e ON l.id_livro = e.id_livro_fk
GROUP BY l.titulo
ORDER BY total_emprestimos DESC;
LIMIT 10; --Corta o resultado para os 10 mais emprestados --

-- View Dashboard Financeiro --
CREATE VIEW vw_dashboard_financeiro AS
SELECT
    COUNT(id_multa) AS total_multas_pagas,
    SUM(valor) AS arrecadacao_total
FROM multas
WHERE pago = 1; -- 1 é TRUE --
