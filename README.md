
# LIBRITECH - SISTEMA DE GESTÃO BIBLIOTECÁRIA (PROJETO BDOO)


Projeto final da disciplina de Banco de Dados Orientado a Objetos.
O sistema implementa regras de negócio, segurança (Roles/Grants) e 
auditoria diretamente no MySQL, utilizando Java (JDBC/Swing) com POO.

## 1. CONFIGURAÇÃO OBRIGATÓRIA 


Para que o sistema funcione, você precisa configurar o Banco e o arquivo
de propriedades.

**PASSO A**: ARQUIVO 'db.properties'
O sistema busca um arquivo de configuração na pasta raiz do projeto
para saber onde o banco está.

Crie um arquivo chamado "db.properties" na raiz (fora da pasta src)
com o seguinte conteúdo:

    user=root
    password=
    useSSL=false


**PASSO B**: BANCO DE DADOS (MYSQL WORKBENCH)
É necessário rodar os scripts SQL na ordem exata abaixo:

1. Execute "schema_completo.sql":
   - Cria o banco, tabelas, procedures, views e triggers.
   - Cria os usuários de segurança (usr_gerente, usr_aluno, etc).

2. Execute "data.sql":
   - Insere os dados de teste.
   - Restaura o Trigger de horário comercial.

## 2. COMO RODAR E ACESSAR

1. Abra o projeto no seu IDE (IntelliJ/Eclipse).
2. Execute a classe principal: application.Main
3. O sistema pedirá o Login do MySQL. Use as credenciais abaixo:

   PERFIL: Gerente (Acesso Total)
   - Usuário: usr_gerente
   - Senha:   senha_foda
   - O que faz: Vê financeiro, backup e auditoria.

   PERFIL: Bibliotecário (Operacional)
   - Usuário: usr_bibliotecario
   - Senha:   senha_chata
   - O que faz: Empresta, renova e cadastra livros/usuários.

   PERFIL: Estagiário (Restrito)
   - Usuário: usr_estagiario
   - Senha:   senha_fraca
   - O que faz: Insere empréstimos, mas NÃO pode excluir livros.

   PERFIL: Aluno (Leitura)
   - Usuário: usr_aluno
   - Senha:   senha_idiota
   - O que faz: Apenas visualiza o acervo e histórico (via Views).


## 3. ROTEIRO DE TESTES


TESTE 1: SEGURANÇA ("A Armadilha do Estagiário")
- Logue como "usr_estagiario".
- Vá em "Acesso Funcionário" > "6. Excluir Livro".
- Tente excluir qualquer livro.
- RESULTADO: O sistema exibe ERRO DE ACESSO NEGADO. O bloqueio é feito
  pelo banco (REVOKE DELETE), provando a segurança no SGBD.

TESTE 2: POO E POLIMORFISMO (JAVA)
- Logue como "usr_gerente".
- Vá em "7. Cadastrar Novo Usuário".
- Cadastre um usuário tipo "ALUNO" e outro tipo "GERENTE".
- RESULTADO: O Java instancia classes diferentes (Student vs Employee)
  e define prazos de devolução diferentes (7 dias vs 14 dias).

TESTE 3: VISUAL E VIEW (ACESSO DO ALUNO)
- Logue como "usr_aluno".
- Vá em "Acesso Aluno" > "1. Consultar Acervo".
- RESULTADO: Abre uma Tabela visual. O "Preço de Custo" e o "Estoque"
  são ocultados pela VIEW do banco de dados.

TESTE 4: HISTÓRICO DE EMPRÉSTIMOS
- No menu do Aluno, vá em "2. Meus Empréstimos".
- Digite o ID: 4 (João Aluno) ou 6 (Pedro Caloteiro).
- RESULTADO: Exibe o histórico do aluno sem dar acesso direto à tabela
  física de empréstimos.

=========================================================================