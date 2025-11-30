package model.entities;

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private double price;
    private int inventory;

    public Book(){

    }

    public Book(String title, String author, String isbn, double price, int inventory) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.inventory = inventory;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public String getAuthor() {return author;}
    public void setAuthor(String author) {this.author = author;}

    public String getIsbn() {return isbn;}
    public void setIsbn(String isbn) {this.isbn = isbn;}

    public double getPrice() {return price;}
    public void setPrice(double price) {this.price = price;}

    public int getInventory() {return inventory;}
    public void setInventory(int inventory) {this.inventory = inventory;}

    @Override
    public String toString() {
        return "Livro: " + title + " | Autor: " + author + " | ISBN: " + isbn;
    }
}

