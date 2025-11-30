package model.enums;

public enum Rule {
    ALUNO("Aluno"),
    GERENTE("Gerente"),
    BIBLIOTECARIO("Bibliotecário"),
    ESTAGIARIO("Estagiário");

    private String description;

    Rule(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
