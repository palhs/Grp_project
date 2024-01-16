package usth.hyperspectral.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import usth.hyperspectral.Entity.*;
import usth.hyperspectral.service.FileUploadService;
import usth.hyperspectral.service.PredictService;

import java.util.UUID;

@ApplicationScoped
public class PredictResource {

    @Inject
    @RestClient
    PredictService predictService;

    @Inject
    FileUploadService fileUploadService;

    @Inject
    SecurityContext securityContext;


    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPredict(Predict predict) {
        try {
            // Call the external POST API using the injected previewService
            Response externalApiResponse = predictService.addPredict(predict);

            // Check if the external API call was successful (HTTP status code 2xx)
            if (externalApiResponse.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                // Extract and return the JSON response from the external API
                PredictResponse predictResponse = externalApiResponse.readEntity(PredictResponse.class);
                String demoPredictPath = predictResponse.getDemo_predict_path();

                // Get the fileLocation
                java.nio.file.Path fileLocation = java.nio.file.Paths.get(demoPredictPath);
                String fileLocationString = fileLocation.toString();

                // Get the file from the fileLocation
                // Check if the file exists
                java.io.File file = fileLocation.toFile();
                if (!file.exists()) {
                    throw new WebApplicationException("File not found at location: " + fileLocationString, Response.Status.NOT_FOUND);
                }


                // Get the fileName
                String fileName = fileLocation.getFileName().toString();

                // Generate unique ID for file
                String fileId = UUID.randomUUID().toString();
                String uniqueFileName = fileId + "_" + fileName;

                // Get user_id from JWT token
                String userId = securityContext.getUserPrincipal().getName();

                if (userId == null || userId.isEmpty()) {
                    throw new WebApplicationException("User ID is missing in the JWT token", Response.Status.UNAUTHORIZED);
                }

                // Find the user in the database
                Users user = Users.findById(Long.parseLong(userId));

                fileUploadService.saveFileToDatabase(fileId, fileLocationString, uniqueFileName, user);


                return Response.ok(file).build();

            } else {
                // Handle non-successful responses from the external API
                return Response.status(externalApiResponse.getStatusInfo())
                        .entity("Error from external API: " + externalApiResponse.readEntity(String.class))
                        .build();
            }
        } catch (Exception e) {
            // Handle exceptions or errors that might occur during the external API call
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing the request: " + e.getMessage())
                    .build();
        }
    }
}

