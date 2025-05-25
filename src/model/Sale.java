package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

public class Sale implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int car_id;
    private int client_id;
    private LocalDate sale_date;
    private double sale_price;
    private Date warranty_end;

    public Sale(int id, int car_id, int client_id, LocalDate sale_date, double sale_price, Date warranty_end) {
        this.id = id;
        this.car_id = car_id;
        this.client_id = client_id;
        this.sale_date = sale_date;
        this.sale_price = sale_price;
        this.warranty_end = warranty_end;
    }
    public Sale(int car_id, int client_id, LocalDate sale_date, double sale_price) {
        this.car_id = car_id;
        this.client_id = client_id;
        this.sale_date = sale_date;
        this.sale_price = sale_price;
    }

    public int getId() {
        return id;
    }
    public int getCar_id() {
        return car_id;
    }
    public int getClient_id() {
        return client_id;
    }
    public LocalDate getSale_date() {
        return sale_date;
    }
    public double getSale_price() {
        return sale_price;
    }

    public void setId(int id) {

    }

    public Date getWarranty_end() {
        return warranty_end;
    }
}
