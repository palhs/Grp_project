package usth.hyperspectral.service;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import usth.hyperspectral.Entity.FileInfo;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.MultivaluedMap;
import usth.hyperspectral.Entity.Users;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class FileUploadService {
    @Inject
    EntityManager entityManager;

    @ConfigProperty(name = "upload.directory")
    String UPLOAD_DIR;
    @Inject
    SecurityContext securityContext;

    @Transactional
    public String uploadFile(MultipartFormDataInput input) {

        List<String> fileIds = new ArrayList<>();

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("file");

        List<String> fileLocations = new ArrayList<>();
        for (InputPart inputPart : inputParts) {
            try {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                String originalFileName = getFileName(header);

                // Generate unique ID for file
                String fileId = UUID.randomUUID().toString();
                String uniqueFileName = fileId + "_" + originalFileName;

                fileIds.add(fileId);
                InputStream inputStream = inputPart.getBody(InputStream.class, null);

                // Write file with uniqueFileName
                writeFile(inputStream, uniqueFileName);

                // Get user_id from JWT token
                String userId = securityContext.getUserPrincipal().getName();

                if (userId == null || userId.isEmpty()) {
                    throw new WebApplicationException("User ID is missing in the JWT token", Response.Status.UNAUTHORIZED);
                }

                // Find the user in the database
                Users user = Users.findById(Long.parseLong(userId));


                // Store file path
                String fileLocation = UPLOAD_DIR + File.separator + uniqueFileName;
                fileLocations.add(fileLocation);

                // Save file info to database
                saveFileToDatabase(fileId, fileLocation, user);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(fileIds.size()).append(" Files Successfully Uploaded. IDs: ").append(fileIds);

        // Append file locations to the result
        resultBuilder.append("\nFile Locations:\n");
        for (String location : fileLocations) {
            resultBuilder.append(location).append("\n");
        }

        return resultBuilder.toString();
    }

    private void writeFile(InputStream inputStream,String fileName)
            throws IOException {
        byte[] bytes = IOUtils.toByteArray(inputStream);
        File customDir = new File(UPLOAD_DIR);
        fileName = customDir.getAbsolutePath() +
                File.separator + fileName;
        Files.write(Paths.get(fileName), bytes,
                StandardOpenOption.CREATE_NEW);
    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.
                getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "";
    }
    @Transactional
    public void saveFileToDatabase(String fileId, String fileLocation, Users user) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.fileId = fileId;
        fileInfo.fileLocation = fileLocation;
        fileInfo.uploadDateTime = LocalDateTime.now(); // Update date and time
        fileInfo.setUser(user); // Set the user who uploaded the file
        fileInfo.persist();
    }
    @Transactional
    public List<FileInfo> getAllFiles() {
        return entityManager.createQuery("SELECT f FROM FileInfo f", FileInfo.class).getResultList();
    }

    @Transactional
    public boolean DeleteFileById(String fileId) {
        FileInfo fileInfo = FileInfo.find("fileId", fileId).firstResult();
        if (fileInfo != null) {
            entityManager.remove(fileInfo);
            return true;
        }
        return false;
    }


//    @Transactional
//    public Response getFileById(String fileId){
//        FileInfo file = FileInfo.find("fileId",fileId).firstResult();
//        String fileLocation = entityManager.createQuery("SELECT fileLocation FROM FileInfo WHERE fileId = file.fileId ");
//    }

//    @Transactional
//    public Response getFileById(String fileId) {
//        FileInfo fileInfo = FileInfo.find("fileId", fileId).firstResult();
//
//        if (fileInfo != null) {
//            Path fileLocation = Paths.get(fileInfo.getFileLocation());
//            // Do something with fileLocation
//            return Response.ok(fileLocation.toFile()).build();
//        } else {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//    }

}