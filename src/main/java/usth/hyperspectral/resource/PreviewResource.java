package usth.hyperspectral.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import usth.hyperspectral.Entity.Preview;
import usth.hyperspectral.service.PreviewService;
import usth.hyperspectral.Entity.PreviewResponse;

@Path("/preview")
public class PreviewResource {
    @Inject
    @RestClient
    PreviewService previewService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPreview(Preview preview) {
        try {
            // Call the external POST API using the injected previewService
            Response externalApiResponse = previewService.addPreview(preview);

            // Check if the external API call was successful (HTTP status code 2xx)
            if (externalApiResponse.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                // Extract and return the JSON response from the external API
                PreviewResponse previewResponse = externalApiResponse.readEntity(PreviewResponse.class);
                String demoPreviewPath = previewResponse.getDemo_preview_path();
                return Response.status(Response.Status.CREATED).entity(previewResponse).build();

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