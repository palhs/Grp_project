package usth.hyperspectral.service;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.*;
import usth.hyperspectral.Entity.Preview;


@Path("/preview")
@RegisterRestClient
public interface PreviewService {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response addPreview (Preview preview);
}

