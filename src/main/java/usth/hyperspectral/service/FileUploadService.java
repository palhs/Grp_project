package usth.hyperspectral.service;

import io.quarkus.panache.common.Parameters;
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
import jakarta.servlet.annotation.MultipartConfig;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Singleton
public class FileUploadService {
    @Inject
    EntityManager entityManager;

    @ConfigProperty(name = "upload.directory")
    String UPLOAD_DIR;
    @Inject
    SecurityContext securityContext;

//    @Transactional
//    public String uploadImg(MultipartFormDataInput input) {
//
//        List<String> fileIds = new ArrayList<>();
//        List<String> fileNames = new ArrayList<>(); // New list to store original file names
//
//        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
//        List<InputPart> inputParts = uploadForm.get("img");
//        List<String> fileLocations = new ArrayList<>();
//
//        for (InputPart inputPart : inputParts) {
//            try {
//                MultivaluedMap<String, String> header = inputPart.getHeaders();
//                String originalFileName = getFileName(header);
//
////                // Check if a file with the same name already exists in the database
////                if (checkFileName(originalFileName)) {
////                    throw new WebApplicationException("A file with the same name already exists", Response.Status.CONFLICT);
////                }
//
//                // Generate unique ID for file
//                String fileId = UUID.randomUUID().toString();
//                String uniqueFileName = fileId + "_" + originalFileName;
//
//                fileIds.add(fileId);
//                fileNames.add(originalFileName); // Add original file name to the list
//
//                try(InputStream inputStream = inputPart.getBody(InputStream.class, null)) {
//                    // Write file with uniqueFileName
//                    writeFile(inputStream, uniqueFileName);
//                }
//
//                // Get user_id from JWT token
//                String userId = securityContext.getUserPrincipal().getName();
//
//                if (userId == null || userId.isEmpty()) {
//                    throw new WebApplicationException("User ID is missing in the JWT token", Response.Status.UNAUTHORIZED);
//                }
//
//                // Find the user in the database
//                Users user = Users.findById(Long.parseLong(userId));
//
//                // Store file path
//                String fileLocation = UPLOAD_DIR + File.separator + uniqueFileName;
//                fileLocations.add(fileLocation);
//
//                // Save file info to database
//                saveFileToDatabase(fileId, fileLocation, originalFileName, user);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        StringBuilder resultBuilder = new StringBuilder();
//        resultBuilder.append(fileIds.size()).append(" Image Uploaded. IDs: ").append(fileIds);
//
//        // Append file names to the result
//        resultBuilder.append("\nFile Names:\n");
//        for (String name : fileNames) {
//            resultBuilder.append(name).append("\n");
//        }
//
//        // Append file locations to the result
//        resultBuilder.append("\nFile Locations:\n");
//        for (String location : fileLocations) {
//            resultBuilder.append(location).append("\n");
//        }
//
//        return resultBuilder.toString();
//    }

    @Transactional
    public Response uploadImg(MultipartFormDataInput input) {

        List<String> fileIds = new ArrayList<>();
        List<String> fileNames = new ArrayList<>(); // New list to store original file names

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("img");
        List<String> fileLocations = new ArrayList<>();
        for (InputPart inputPart : inputParts) {
            try {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                String originalFileName = getFileName(header);


                // Get user_id from JWT token
                String userId = securityContext.getUserPrincipal().getName();

                if (userId == null || userId.isEmpty()) {
                    throw new WebApplicationException("User ID is missing in the JWT token", Response.Status.UNAUTHORIZED);
                }

                // Check if a file with the same name already exists in the database
//                if (checkFileName(originalFileName, Long.parseLong(userId))) {
//                    throw new WebApplicationException("A file with the same name already exists", Response.Status.CONFLICT);
//                }

                // Generate unique ID for file
                String fileId = UUID.randomUUID().toString();
//                String uniqueFileName = fileId + "_" + originalFileName;

                fileIds.add(fileId);
                fileNames.add(originalFileName); // Add original file name to the list

                try (InputStream inputStream = new BufferedInputStream(inputPart.getBody(InputStream.class, null))) {
                    // Check if the file is a zip file and unzip it
                    if (originalFileName.endsWith(".zip")) {
                        unzipAndProcessFiles(inputStream, fileId);
                    } else {
                        // Write file with originalFileName
                        writeFile(inputStream, originalFileName);
                    }
                }

                // Find the user in the database
                Users user = Users.findById(Long.parseLong(userId));

                // Store file path
                String fileLocation = Paths.get(UPLOAD_DIR, originalFileName).toAbsolutePath().toString();
                fileLocations.add(fileLocation);

                // Save file info to database
                saveFileToDatabase(fileId, fileLocation, originalFileName, user,"upload");

            } catch (WebApplicationException e) {
                return Response.status(e.getResponse().getStatus())
                        .entity(e.getMessage())
                        .build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error processing the request: " + e.getMessage())
                        .build();
            }
        }

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(fileIds.size()).append(" Image Uploaded. IDs: ").append(fileIds);

        // Append file names to the result
        resultBuilder.append("\nFile Names:\n");
        for (String name : fileNames) {
            resultBuilder.append(name).append("\n");
        }

        // Append file locations to the result
        resultBuilder.append("\nFile Locations:\n");
        for (String location : fileLocations) {
            resultBuilder.append(location).append("\n");
        }

        return Response.ok(resultBuilder.toString()).build();
    }

//    @Transactional
//    public String uploadHDR(MultipartFormDataInput input) {
//
//        List<String> fileIds = new ArrayList<>();
//        List<String> fileNames = new ArrayList<>(); // New list to store original file names
//
//        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
//        List<InputPart> inputParts = uploadForm.get("hdr");
//
//        List<String> fileLocations = new ArrayList<>();
//        for (InputPart inputPart : inputParts) {
//            try {
//                MultivaluedMap<String, String> header = inputPart.getHeaders();
//                String originalFileName = getFileName(header);
//
////                // Check if a file with the same name already exists in the database
////                if (checkFileName(originalFileName)) {
////                    throw new WebApplicationException("A file with the same name already exists", Response.Status.CONFLICT);
////                }
//
//                // Generate unique ID for file
//                String fileId = UUID.randomUUID().toString();
//                String uniqueFileName = fileId + "_" + originalFileName;
//
//                fileIds.add(fileId);
//                fileNames.add(originalFileName); // Add original file name to the list
//
//                try(InputStream inputStream = inputPart.getBody(InputStream.class, null)) {
//                    // Write file with uniqueFileName
//                    writeFile(inputStream, uniqueFileName);
//
//                }
//
//                // Get user_id from JWT token
//                String userId = securityContext.getUserPrincipal().getName();
//
//                if (userId == null || userId.isEmpty()) {
//                    throw new WebApplicationException("User ID is missing in the JWT token", Response.Status.UNAUTHORIZED);
//                }
//
//                // Find the user in the database
//                Users user = Users.findById(Long.parseLong(userId));
//
//                // Store file path
//                String fileLocation = UPLOAD_DIR + File.separator + uniqueFileName;
//                fileLocations.add(fileLocation);
//
//                // Save file info to database
//                saveFileToDatabase(fileId, fileLocation, originalFileName, user);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        StringBuilder resultBuilder = new StringBuilder();
//        resultBuilder.append(fileIds.size()).append(" HDR Uploaded. IDs: ").append(fileIds);
//
//        // Append file names to the result
//        resultBuilder.append("\nFile Names:\n");
//        for (String name : fileNames) {
//            resultBuilder.append(name).append("\n");
//        }
//
//        // Append file locations to the result
//        resultBuilder.append("\nFile Locations:\n");
//        for (String location : fileLocations) {
//            resultBuilder.append(location).append("\n");
//        }
//
//        return resultBuilder.toString();
//    }

    @Transactional
    public Response uploadHDR(MultipartFormDataInput input) {

        List<String> fileIds = new ArrayList<>();
        List<String> fileNames = new ArrayList<>(); // New list to store original file names

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("hdr");
        List<String> fileLocations = new ArrayList<>();
        for (InputPart inputPart : inputParts) {
            try {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                String originalFileName = getFileName(header);

                // Get user_id from JWT token
                String userId = securityContext.getUserPrincipal().getName();

                if (userId == null || userId.isEmpty()) {
                    throw new WebApplicationException("User ID is missing in the JWT token", Response.Status.UNAUTHORIZED);
                }

                // Check if a file with the same name already exists in the database
//                if (checkFileName(originalFileName, Long.parseLong(userId))) {
//                    throw new WebApplicationException("A file with the same name already exists", Response.Status.CONFLICT);
//                }

                // Generate unique ID for file
                String fileId = UUID.randomUUID().toString();
                String uniqueFileName = fileId + "_" + originalFileName;

                fileIds.add(fileId);
                fileNames.add(originalFileName); // Add original file name to the list

                try (InputStream inputStream = new BufferedInputStream(inputPart.getBody(InputStream.class, null))) {
                    // Check if the file is a zip file and unzip it
                    if (originalFileName.endsWith(".zip")) {
                        unzipAndProcessFiles(inputStream, fileId);
                    } else {
                        // Write file with originalFileName
                        writeFile(inputStream, uniqueFileName);
                    }
                }

                // Find the user in the database
                Users user = Users.findById(Long.parseLong(userId));

                // Store file path
                String fileLocation = Paths.get(UPLOAD_DIR, originalFileName).toAbsolutePath().toString();
                fileLocations.add(fileLocation);

                // Save file info to database
                saveFileToDatabase(fileId, fileLocation, originalFileName, user,"upload");

            } catch (WebApplicationException e) {
                return Response.status(e.getResponse().getStatus())
                        .entity(e.getMessage())
                        .build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error processing the request: " + e.getMessage())
                        .build();
            }
        }

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(fileIds.size()).append(" Image Uploaded. IDs: ").append(fileIds);

        // Append file names to the result
        resultBuilder.append("\nFile Names:\n");
        for (String name : fileNames) {
            resultBuilder.append(name).append("\n");
        }

        // Append file locations to the result
        resultBuilder.append("\nFile Locations:\n");
        for (String location : fileLocations) {
            resultBuilder.append(location).append("\n");
        }

        return Response.ok(resultBuilder.toString()).build();
    }

//    private void writeFile(InputStream inputStream, String fileName) throws IOException {
//        File customDir = new File(UPLOAD_DIR);
//        fileName = customDir.getAbsolutePath() + File.separator + fileName;
//        try (OutputStream outputStream = Files.newOutputStream(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
//            byte[] buffer = new byte[1024 * 1024]; // Buffer size of 1MB
//            int bytesRead;
//            while ((bytesRead = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
//            }
//        } catch (IOException e) {
//            // Handle IOException appropriately
//            e.printStackTrace(); // Example: Print the stack trace for debugging
//            throw e; // Rethrow the exception or handle it based on your requirements
//        }
//        finally{
//            if(inputStream != null){
//                try{
//                    inputStream.close();
//                } catch (IOException e){
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    private synchronized void writeFile(InputStream inputStream, String fileName) throws IOException {
        File customDir = new File(UPLOAD_DIR);
        File file = new File(customDir, fileName);

        byte[] buffer = null;
        try (OutputStream outputStream = Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
             InputStream in = new BufferedInputStream(inputStream)) {
            buffer = new byte[1024 * 1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // Handle IOException appropriately
            e.printStackTrace(); // Example: Print the stack trace for debugging
            throw e; // Rethrow the exception or handle it based on your requirements
        } finally {
            buffer = null;
            System.gc();
        }
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
    public synchronized void saveFileToDatabase(String fileId, String fileLocation, String fileName, Users user, String type) {
        try {
            FileInfo fileInfo = new FileInfo();
            fileInfo.fileId = fileId;
            fileInfo.fileLocation = fileLocation;
            fileInfo.fileName = fileName;
            fileInfo.uploadDateTime = LocalDateTime.now(); // Update date and time
            fileInfo.type = type;
            fileInfo.setUser(user); // Set the user who uploaded the file
            fileInfo.persist();
        } catch (Exception e){
            e.printStackTrace();
        }

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

    @Transactional
    public boolean checkFileName(String fileName, Long user_id) {
        // Query the database to find a file with the given name
        FileInfo fileInfo = FileInfo.find("fileName = :fileName and user_id = :user_id", Parameters.with("fileName", fileName).and("user_id", user_id)).firstResult();

        // If fileInfo is not null, a file with the given name exists in the database
        return fileInfo != null;
    }

//    private void unzipAndProcessFiles(InputStream zipInputStream, String fileId) throws IOException {
//        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
//            ZipEntry zipEntry;
//            byte[] buffer = new byte[8 * 1024 * 1024]; // 1MB buffer (adjust as needed)
//
//            while ((zipEntry = zis.getNextEntry()) != null) {
//                String entryName = zipEntry.getName();
//                String uniqueFileName = fileId + "_" + entryName;
//
//                // Process each entry in a streaming fashion
//                try (OutputStream bos = Files.newOutputStream(Paths.get(UPLOAD_DIR, uniqueFileName),
//                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
//
//                    int bytesRead;
//                    while ((bytesRead = zis.read(buffer)) != -1) {
//                        bos.write(buffer, 0, bytesRead);
//                    }
//                }
//            }
//        }
//    }


    private synchronized void unzipAndProcessFiles(InputStream zipInputStream, String fileId) throws IOException {
        byte[] buffer = new byte[1024 * 1024]; // 1MB buffer
        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry zipEntry;

            while ((zipEntry = zis.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                String uniqueFileName = fileId + "_" + entryName;

                // Process each entry in a streaming fashion
                try (OutputStream bos = Files.newOutputStream(Paths.get(UPLOAD_DIR, uniqueFileName),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                    int bytesRead;
                    while ((bytesRead = zis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new WebApplicationException("Error processing zip file: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

//    @Transactional
//    public Response getFileById(String fileId){
//        FileInfo file = FileInfo.find("fileId",fileId).firstResult();
//        String fileLocation = entityManager.createQuery("SELECT fileLocation FROM FileInfo WHERE fileId = file.fileId ");
//    }
//
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