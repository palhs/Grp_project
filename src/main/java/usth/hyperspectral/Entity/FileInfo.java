package usth.hyperspectral.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class FileInfo extends PanacheEntity {

    @Column(name = "file_id", unique = true)
    public String fileId;
    @Column(name = "file_location")
    public String fileLocation;
    @Column(name = "upload_date_time")
    public LocalDateTime uploadDateTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    public FileInfo(String fileId, String fileLocation, Users user) {
        this.fileId = fileId;
        this.fileLocation = fileLocation;
        this.uploadDateTime = LocalDateTime.now();
        this.user = user;
    }

    public FileInfo() {

    }
    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public LocalDateTime getUploadDateTime() {
        return uploadDateTime;
    }

    public void setUploadDateTime(LocalDateTime uploadDateTime) {
        this.uploadDateTime = uploadDateTime;
    }
}
