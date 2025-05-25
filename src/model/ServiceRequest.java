package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ServiceRequest implements Serializable {
    private int id;
    private int clientId;
    private Integer carId; // может быть null
    private String description;
    private LocalDateTime requestDate;
    private String status;

    public ServiceRequest(int id, int clientId, Integer carId, String description, LocalDateTime requestDate, String status) {
        this.id = id;
        this.clientId = clientId;
        this.carId = carId;
        this.description = description;
        this.requestDate = requestDate;
        this.status = status;
    }

    public ServiceRequest() {

    }

    // Геттеры и сеттеры

    public int getId() { return id; }
    public int getClientId() { return clientId; }
    public Integer getCarId() { return carId; }
    public String getDescription() { return description; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public void setCarId(Integer carId) { this.carId = carId; }
    public void setDescription(String description) { this.description = description; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public void setStatus(String status) { this.status = status; }
}
