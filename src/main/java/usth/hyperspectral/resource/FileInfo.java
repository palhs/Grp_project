package usth.hyperspectral.resource;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class FileInfo extends PanacheEntity {

    public String fileId;
    public String fileLocation;
    public LocalDateTime uploadDateTime;

    public FileInfo(String fileId, String fileLocation) {
        this.fileId = fileId;
        this.fileLocation = fileLocation;
        this.uploadDateTime = LocalDateTime.now();
    }

    public FileInfo() {

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
