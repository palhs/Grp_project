package usth.hyperspectral.Controller;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import usth.hyperspectral.Entity.Users;
import usth.hyperspectral.resource.LoginResponse;
import usth.hyperspectral.service.JwtService;
import usth.hyperspectral.service.UserService;

import java.net.URI;
import java.util.Objects;

@Path("/user")
public class UserController {

    @Inject
    UserService userService;

    @GET
    @Path("/get")
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUser(){
        return Response.ok(userService.getAllUser()).build();
    }

    @Path("/register")
    @POST
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(Users user){
        Users newUser = userService.addUser(user);
        if(newUser != null){
            return Response.created(URI.create("/user/" + newUser.user_id))
                    .entity("User created successfully")
                    .build();
        }else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Username already exists. Choose a different username.")
                    .build();
        }
    }

    @GET
    @RolesAllowed({"admin"})
    @Path("/get/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findUser(@PathParam("id") Long user_id){
        return Response.ok(userService.findUser(user_id)).build();
    }

    @PUT
    @RolesAllowed({"user"})
    @Path("/put/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@Context SecurityContext securityContext, Users updatedUser) {
        Long user_id = Long.parseLong(securityContext.getUserPrincipal().getName());
        updatedUser = userService.updateUser(user_id, updatedUser);
        if(updatedUser != null){
            return Response.created(URI.create("/user/" + user_id))
                    .entity("User's password updated successfully")
                    .build();
        }else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Path("/delete/{id}")
    @DELETE
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") Long id){
        boolean isDeleted = userService.deleteUser(id);
        if (isDeleted){
            return Response.ok()
                    .entity("User with id " + id + " deleted successfully")
                    .build();
        }
        else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    @Inject
    JwtService jwtService;
    @POST
    @Path("/login")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Users loginRequest) {
        // Retrieve the user from the database based on the provided username
        Users user = Users.find("username", loginRequest.getUsername()).firstResult();
        Long userId = user.getUser_id();
        String username = user.getUsername();

        // Check if the user exists and the password matches
        if (user != null && BcryptUtil.matches(loginRequest.getPassword(), user.getPassword())) {
            String jwtToken;
            if (Objects.equals(user.getRole(), "admin")) {
                // Generate admin JWT token
                jwtToken = jwtService.generateAdminJwt(userId,username);
            } else {
                // Generate user JWT token
                jwtToken = jwtService.generateUserJwt(userId,username);
            }

            // Return the JWT token in a LoginResponse
            LoginResponse loginResponse = new LoginResponse(jwtToken);
            return Response.ok(loginResponse).build();
        } else {
            // Invalid credentials
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}