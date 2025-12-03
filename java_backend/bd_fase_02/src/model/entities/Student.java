package model.entities;

public class Student extends User {

    public Student() {

    }

    @Override
    public int getLoanDeadlineDays() {
        return 7;
    }
}