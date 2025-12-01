USE libritech;

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

DROP TRIGGER IF EXISTS trg_trava_horario_comercial;

TRUNCATE TABLE multas;
TRUNCATE TABLE emprestimos;
TRUNCATE TABLE enderecos;
TRUNCATE TABLE livros;
TRUNCATE TABLE usuarios;
TRUNCATE TABLE log_auditoria;

-- 4. INSERINDO USUARIOS (Com IDs fixos)
INSERT INTO usuarios (id_usuario, nome, cpf, email, senha, tipo) VALUES
(1, 'Ana Gerente', '11122233344', 'ana.gerente@libritech.com', 'senha_foda', 'GERENTE'),
(2, 'Carlos Bibliotecario', '22233344455', 'carlos.biblio@libritech.com', 'senha_chata', 'BIBLIOTECARIO'),
(3, 'Lucas Estagiario', '33344455566', 'lucas.estag@libritech.com', 'senha_fraca', 'ESTAGIARIO'),
(4, 'João Aluno', '44455566677', 'joao.aluno@email.com', 'senha_idiota', 'ALUNO'),
(5, 'Maria Estudiosa', '55566677788', 'maria.estudiosa@email.com', 'senha_idiota', 'ALUNO'),
(6, 'Pedro Caloteiro', '66677788899', 'pedro.caloteiro@email.com', 'senha_idiota', 'ALUNO');

-- 5. INSERINDO ENDEREÇOS
INSERT INTO enderecos (logradouro, bairro, cidade, uf, id_usuario_fk) VALUES
('Rua da Gestão, 100', 'Centro', 'São Paulo', 'SP', 1),
('Av. dos Livros, 200', 'Jardim Leitura', 'Campinas', 'SP', 2),
('Rua do Café, 303', 'Universitário', 'Curitiba', 'PR', 3),
('Travessa do Aprender, 40', 'Escolar', 'Rio de Janeiro', 'RJ', 4),
('Alameda das Notas, 10', 'Vila Madalena', 'São Paulo', 'SP', 5),
('Beco da Dívida, 00', 'Perdidos', 'Belo Horizonte', 'MG', 6);

-- 6. INSERINDO LIVROS (Com IDs fixos)
INSERT INTO livros (id_livro, titulo, autor, isbn, preco_custo, quantidade_estoque, status) VALUES
(1, 'Engenharia de Software Moderna', 'Marco Tulio Valente', '9786586110166', 89.90, 5, 'DISPONIVEL'),
(2, 'Clean Code', 'Robert C. Martin', '9788576082675', 95.00, 3, 'DISPONIVEL'),
(3, 'O Senhor dos Anéis', 'J.R.R. Tolkien', '9788595084742', 59.90, 0, 'DISPONIVEL'), -- Estoque 0 de propósito
(4, 'Dom Casmurro', 'Machado de Assis', '9788572325001', 25.50, 10, 'DISPONIVEL'),
(5, 'Harry Potter e a Pedra Filosofal', 'J.K. Rowling', '9788532530783', 45.00, 2, 'DISPONIVEL'),
(6, 'Entendendo Algoritmos', 'Aditya Bhargava', '9788575225639', 60.00, 1, 'DISPONIVEL');

-- 7. INSERINDO EMPRESTIMOS (Com IDs fixos para garantir as multas)

INSERT INTO emprestimos (id_emprestimo, id_usuario_fk, id_livro_fk, data_saida, data_prevista, data_devolucao) VALUES
-- Empréstimo 1: João pegou e devolveu (Sem multa)
(1, 4, 1, '2023-10-01 10:00:00', '2023-10-08', '2023-10-07 14:00:00'),

-- Empréstimo 2: Maria pegou e devolveu (Sem multa)
(2, 5, 5, '2023-10-05 09:00:00', '2023-10-12', '2023-10-12 16:00:00'),

-- Empréstimo 3: Maria pegou outro livro e AINDA ESTÁ com ele (Dentro do prazo)
(3, 5, 2, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), NULL),

-- Empréstimo 4: Pedro pegou, ATRASOU MUITO e ainda não devolveu (Gera multa pendente)
(4, 6, 6, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), NULL),

-- Empréstimo 5: João atrasou no passado, mas devolveu (Gerou multa paga)
(5, 4, 5, '2023-09-01 10:00:00', '2023-09-08', '2023-09-10 10:00:00');

-- 8. INSERINDO MULTAS (Amarradas nos IDs de Emprestimos acima)
INSERT INTO multas (id_emprestimo_fk, valor, pago) VALUES
(4, 26.00, FALSE), -- Multa do Pedro (Emprestimo 4) que ainda deve
(5, 4.00, TRUE);   -- Multa do João (Emprestimo 5) que já pagou

-- 9. REATIVANDO O TRIGGER DE HORÁRIO
DELIMITER $$
CREATE TRIGGER trg_trava_horario_comercial
BEFORE INSERT ON emprestimos
FOR EACH ROW
BEGIN
    IF HOUR(CURRENT_TIME()) < 8 OR HOUR(CURRENT_TIME()) >= 18 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = "ERRO: Operacoes permitidas apenas em horario comercial (8h-18h).";
    END IF;
END$$
DELIMITER ;

-- 10. Reativa as checagens de segurança
SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Banco de dados populado com sucesso e trigger reativado!' AS Resultado;