package model;

import java.io.Serializable;

public class Car implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String VIN;
    private String brand;
    private String model;
    private int year;
    private double price;
    private int warrantyYears;
    private int quantity;
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

    public Car(int id, String vin, String brand, String modelCar, int year, double price, int warrantyYears, boolean isAvailable, int quantity) {
        this.id = id;
        this.VIN = vin;
        this.brand = brand;
        this.model = modelCar;
        this.year = year;
        this.price = price;
        this.warrantyYears = warrantyYears;
        this.available = isAvailable;
        this.quantity = quantity;
    }
    public Car(String vin, String brand, String modelCar, int year, double price, int warrantyYears, boolean isAvailable, int quantity) {
        this.VIN = vin;
        this.brand = brand;
        this.model = modelCar;
        this.year = year;
        this.price = price;
        this.warrantyYears = warrantyYears;
        this.available = isAvailable;
        this.quantity = quantity;
    }

    public Car(int id, String brand, String model, int year) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
    }

    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public double getPrice() { return price; }
    public int getWarrantyYears() { return warrantyYears; }
    public boolean isAvailable() { return available; }

    public int getId() { return id; }
    public String getVIN() { return VIN; }
    public int getQuantity() { return quantity; }

    public void setId(int carIdToEdit) {
        this.id = carIdToEdit;
    }
    public void setVIN(String VIN) {
        this.VIN = VIN;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    @Override
    public String toString() {
        return brand + " " + model + " " + year;
    }
}
