package model;

import java.io.Serializable;
import java.time.LocalDate;

public class ServiceRecord implements Serializable {
    private int id;
    private int car_id;
    private int client_id;
    private LocalDate service_date;
    private String description;
    private boolean is_under_warranty;

    public ServiceRecord(int id, int car_id, int client_id, LocalDate service_date, String description, boolean is_under_warranty) {
        this.id = id;
        this.car_id = car_id;
        this.client_id = client_id;
        this.service_date = service_date;
        this.description = description;
        this.is_under_warranty = is_under_warranty;
    }
    public ServiceRecord(int car_id, int client_id, LocalDate service_date, String description, boolean is_under_warranty) {
        this.car_id = car_id;
        this.client_id = client_id;
        this.service_date = service_date;
        this.description = description;
        this.is_under_warranty = is_under_warranty;
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
    public LocalDate getService_date() {
        return service_date;
    }
    public String getDescription() {
        return description;
    }
    public boolean is_under_warranty() {
        return is_under_warranty;
    }


    public void setId(int id) {
    }
}
