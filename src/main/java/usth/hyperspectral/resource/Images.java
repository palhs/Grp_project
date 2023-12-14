package usth.hyperspectral.resource;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
public class Images extends PanacheEntityBase {

    @Id
    Long imageID;

    String imageLocation;

    @ManyToOne
    @JoinColumn(name = "user_id")
    Users user;

    String season;

    public LocalDateTime uploadDateTime;

    public Images(Long imageID, String imageLocation, Users user, String season, LocalDateTime uploadDateTime) {
        this.imageID = imageID;
        this.imageLocation = imageLocation;
        this.user = user;
        this.season = season;
        this.uploadDateTime = uploadDateTime;
    }

    public Images() {

    }

    public Long getImageID() {
        return imageID;
    }

    public void setImageID(Long imageID) {
        this.imageID = imageID;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public void setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public LocalDateTime getUploadDateTime() {
        return uploadDateTime;
    }

    public void setUploadDateTime(LocalDateTime uploadDateTime) {
        this.uploadDateTime = uploadDateTime;
    }
}
