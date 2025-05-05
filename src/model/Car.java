package model;

import java.io.Serializable;

public class Car implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String brand;
    private String model;
    private int year;
    private double price;
    private int warrantyYears;
    private boolean available;

    public Car(String brand, String model, int year, double price, int warrantyYears, boolean available) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.price = price;
        this.warrantyYears = warrantyYears;
        this.available = available;
    }

    public Car(int id, String brand, String model, int year, double price, int warrantyYears, boolean available) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.price = price;
        this.warrantyYears = warrantyYears;
        this.available = available;
    }

    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public double getPrice() { return price; }
    public int getWarrantyYears() { return warrantyYears; }
    public boolean isAvailable() { return available; }

    public int getId() { return id; }
}
