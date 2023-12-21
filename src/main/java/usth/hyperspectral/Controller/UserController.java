package usth.hyperspectral.Controller;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import usth.hyperspectral.resource.LoginResponse;
import usth.hyperspectral.Entity.Users;
import usth.hyperspectral.service.JwtService;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Path("/user")
@ApplicationScoped
public class UserController {

    @Inject
    JwtService jwtService;



    @GET
    @Path("/get")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUser(){
        List<Users> usersList = Users.listAll();
        return Response.ok(usersList).build();
    }

    @Path("/register")
    @POST
    @PermitAll
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(Users user){
        user.setPassword(BcryptUtil.bcryptHash(user.getPassword()));
        Users.persist(user);
        if(user.isPersistent()){
            return Response.created(URI.create("/user/" + user.user_id)).build();
        }else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @RolesAllowed({"admin","user"})
    @Path("/get/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findUser(@PathParam("id") Long user_id){
        Users user = Users.findById(user_id);
        return Response.ok(user).build();
    }

    @PUT
    @RolesAllowed({"admin","user"})
    @Transactional
    @Path("/put/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("id") Long user_id ){
        Optional<Users> optionalUsers = Users.findByIdOptional(user_id);
        if(optionalUsers.isPresent()){
            Users dbUser = optionalUsers.get();

            if(Objects.nonNull(dbUser.getPassword())){
                dbUser.setPassword(dbUser.getPassword());
            }

            dbUser.persist();
            if(dbUser.isPersistent()){
                return Response.created(URI.create("/user/" + user_id)).build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }
        else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Path("/delete/{id}")
    @DELETE
    @RolesAllowed({"admin"})
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") Long id){
        boolean isDeleted = Users.deleteById(id);
        if (isDeleted){
            return Response.noContent().build();
        }
        else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Users loginRequest) {
        // Retrieve the user from the database based on the provided username
        Users user = Users.find("username", loginRequest.getUsername()).firstResult();
        Long userId = user.getUser_id();

        // Check if the user exists and the password matches
        if (user != null && BcryptUtil.matches(loginRequest.getPassword(), user.getPassword())) {
            String jwtToken;
            if (Objects.equals(user.getRole(), "admin")) {
                // Generate admin JWT token
                jwtToken = jwtService.generateAdminJwt(userId);
            } else {
                // Generate user JWT token
                jwtToken = jwtService.generateUserJwt(userId);
            }

            // Return the JWT token in a LoginResponse
            LoginResponse loginResponse = new LoginResponse(jwtToken);
            return Response.ok(loginResponse).build();
        } else {
            // Invalid credentials
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }


    // Additional classes for request/response



}
