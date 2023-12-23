package usth.hyperspectral.Controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
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
    @RolesAllowed({"admin"})
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
        if(isUsernameDuplicate(user.getUsername())){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Username already exists. Choose a different username.")
                    .build();
        }
        user.setPassword(BcryptUtil.bcryptHash(user.getPassword()));
        user.setRole("user");
        Users.persist(user);
        if(user.isPersistent()){
            return Response.created(URI.create("/user/" + user.user_id))
                    .entity("User created successfully")
                    .build();
        }else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @RolesAllowed({"admin"})
    @Path("/get/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findUser(@PathParam("id") Long user_id){
        Users user = Users.findById(user_id);
        return Response.ok(user).build();
    }

    @PUT
    @RolesAllowed({"user"})
    @Transactional
    @Path("/put/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@Context SecurityContext securityContext, Users updatedUser) {
        Long user_id = Long.parseLong(securityContext.getUserPrincipal().getName());
        Users existingUser = Users.findById(user_id);

        if (existingUser != null) {
            // Update only the relevant fields, e.g., password
            if (Objects.nonNull(updatedUser.getPassword())) {
                existingUser.setPassword(BcryptUtil.bcryptHash(updatedUser.getPassword()));
            }
            existingUser.persist();

            if (existingUser.isPersistent()) {
                return Response.created(URI.create("/user/" + user_id))
                        .entity("User's password updated successfully")
                        .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
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
            return Response.ok()
                            .entity("User with id " + id + " deleted successfully")
                            .build();
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


    // Helper method to check if the username already exists
    private boolean isUsernameDuplicate(String username) {
        try {
            // Query the database for the username
            Users existingUser = Users.find("username", username).firstResult();
            return existingUser != null;
        } catch (NoResultException e) {
            // No user found with the given username, not a duplicate
            return false;
        }
    }



}
