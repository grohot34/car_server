package model;
import java.io.Serializable;
import java.time.LocalDate;

public class Review implements Serializable {
    private int id;
    private int clientId;
    private String description;
    private int rating;
    private LocalDate dateReview;

    public Review(int id,int clientId, String description, int rating, LocalDate dateReview) {
        this.id = id;
        this.clientId = clientId;
        this.description = description;
        this.rating = rating;
        this.dateReview = LocalDate.now();
    }

    public Review(int clientId, String description, int rating) {
        this.clientId = clientId;
        this.description = description;
        this.rating = rating;
        this.dateReview = LocalDate.now();
    }
    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }
    public String getDescription() {
        return description;
    }
    public int getRating() {
        return rating;
    }
    public LocalDate getDateReview() {
        return dateReview;
    }
}