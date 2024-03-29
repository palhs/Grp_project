package usth.hyperspectral.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class FileInfo extends PanacheEntity {

    @Column(name = "file_name")
    @NotNull
    public String fileName;

    @Column(name = "file_id", unique = true)
    public String fileId;
    @Column(name = "file_location")
    public String fileLocation;
    @Column(name = "upload_date_time")
    public LocalDateTime uploadDateTime;

    @Column(name = "type")
    public String type;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    public FileInfo(String fileName, String fileId, String fileLocation, LocalDateTime uploadDateTime, Users user, String type) {
        this.fileName = fileName;
        this.fileId = fileId;
        this.fileLocation = fileLocation;
        this.uploadDateTime = uploadDateTime;
        this.type = type;
        this.user = user;
    }


    public FileInfo() {
    }

    public String getFileName() {
        return fileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
