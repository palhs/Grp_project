package usth.hyperspectral.resource;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import usth.hyperspectral.service.JwtService;



//@Path("/jwt")
//@ApplicationScoped
public class JwtResource {

    @Inject
    JwtService service;

//    @GET
//    @Path("/admin")
//    @Produces(MediaType.TEXT_PLAIN)
    public Response getAdminJwt() {
        String jwt = service.generateAdminJwt();
        return Response.ok(jwt).build();
    }

//    @GET
//    @Path("/user")
//    @Produces(MediaType.TEXT_PLAIN)
    public Response getUserJwt() {
        String jwt = service.generateUserJwt();
        return Response.ok(jwt).build();
    }
}
