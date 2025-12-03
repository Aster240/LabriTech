package model.entities;

import model.enums.Rule;

public abstract class User { // Garanta que é abstract

    protected Integer id;
    protected String name;
    protected String cpf;
    protected String email;
    protected String password;
    protected Rule rule;

    public abstract int getLoanDeadlineDays();

    public User() {
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Rule getRule() {
        return rule;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}