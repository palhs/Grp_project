package usth.hyperspectral.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import usth.hyperspectral.resource.Users;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Path("/user")
@ApplicationScoped
public class UserService {

    @Inject
    JwtService service;

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
    public Response login(LoginRequest loginRequest) {
        // Retrieve the user from the database based on the provided username
        Users user = Users.find("username", loginRequest.getUsername()).firstResult();

        // Check if the user exists and the password matches
        if (user != null && BcryptUtil.matches(loginRequest.getPassword(), user.getPassword())) {
            if(Objects.equals(user.getRole(), "admin")) {
                // Return the user's role
                return Response.ok(service.generateAdminJwt()).build();
            }
            else{
                return Response.ok(service.generateUserJwt()).build();
            }
        } else {
            // Invalid credentials
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }


    // Additional classes for request/response

    @RegisterForReflection
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and setters...

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
