package model.entities;

public class Employee extends User {

    @Override
    public int getLoanDeadlineDays() { // vantagem por ser funcionário
        return 14;
    }
}