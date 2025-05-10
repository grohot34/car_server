package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

public class PurchaseInfo implements Serializable {
    private int id;
    private int carId;
    private String brand;
    private String model;
    private int year;
    private double totalPrice;
    private String paymentMethod;
    private LocalDate orderDate;
    private LocalDate warranty;
    private LocalDate warranty_date;

    public PurchaseInfo(String brand, String model, int year, double totalPrice, String paymentMethod, LocalDate orderDate) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.orderDate = orderDate;
    }

    public PurchaseInfo(int carId, String brand, String model, int year,
                        double totalPrice, String paymentMethod, LocalDate orderDate) {
        this.carId = carId;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.orderDate = orderDate;
    }

    // Геттеры
    public int getId() {
        return id;
    }
    public int getCar_id() {
        return carId;
    }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public double getTotalPrice() { return totalPrice; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDate getOrderDate() { return orderDate; }
}
