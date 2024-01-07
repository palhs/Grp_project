package usth.hyperspectral.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import usth.hyperspectral.Entity.Predict;
import usth.hyperspectral.Entity.PredictResponse;
import usth.hyperspectral.Entity.Preview;
import usth.hyperspectral.Entity.PreviewResponse;
import usth.hyperspectral.service.PredictService;

@Path("/predict")
public class PredictResource {

    @Inject
    @RestClient
    PredictService predictService;

    @POST
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
                return Response.status(Response.Status.CREATED).entity(predictResponse).build();

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
