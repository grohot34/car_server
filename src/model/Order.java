package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private int clientId;
    private int carId;
    private LocalDate orderDate;
    private String status;
    private String paymentMethod;
    private double totalPrice;

    public Order(int clientId, int carId, LocalDate orderDate, String status, String paymentMethod, double totalPrice) {
        this.clientId = clientId;
        this.carId = carId;
        this.orderDate = orderDate;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
    }

    public int getClientId() { return clientId; }
    public int getCarId() { return carId; }
    public LocalDate getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getTotalPrice() { return totalPrice; }
}
