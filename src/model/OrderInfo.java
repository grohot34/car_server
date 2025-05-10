package model;

import java.io.Serializable;
import java.time.LocalDate;

public class OrderInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    int clientId;
    int carId;
    private String brand;
    private String model;
    private int year;
    private double totalPrice;
    private String paymentMethod;
    private LocalDate orderDate;
    private String status;

    public OrderInfo(int id, String brand, String model, int year, double totalPrice, String paymentMethod, LocalDate orderDate, String status) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.orderDate = orderDate;
        this.status = status;
    }
    public OrderInfo() {
        super();
    }

    public OrderInfo(int id, int clientId, String brand, String model, int year, double totalPrice, String paymentMethod, LocalDate orderDate, String status) {
        this.id = id;
        this.clientId = clientId;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.orderDate = orderDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }
    public int getClientId() {
        return clientId;
    }

    public String getBrand() {
        return brand;
    }
    public String getModel() {
        return model;
    }
    public int getYear() {
        return year;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public LocalDate getOrderDate() {
        return orderDate;
    }
    public String getStatus() {
        return status;
    }
    public int getCarId() {
        return carId;
    }
}