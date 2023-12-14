package usth.hyperspectral.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.swing.text.html.parser.Entity;

@Singleton
public class UploadImageService {

    @Inject
    EntityManager entityManager;

    @ConfigProperty(name = "upload.directory")
    String UPLOAD_DIR;


}
