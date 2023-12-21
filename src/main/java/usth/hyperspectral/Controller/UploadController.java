package usth.hyperspectral.Controller;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import usth.hyperspectral.Entity.FileInfo;
import usth.hyperspectral.service.FileUploadService;

import java.nio.file.Paths;
import java.util.List;

@Path("/file")
public class UploadController {
    @Inject
    FileUploadService fileUploadService;

    @POST
    @RolesAllowed({"user"})
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response fileUpload(@MultipartForm MultipartFormDataInput input) {
        return Response.ok().entity(fileUploadService.uploadFile(input)).build();
    }

    //Get all files information
    @GET
    @Path("/get")
    @RolesAllowed({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public List<FileInfo> getAllFiles() {
        return fileUploadService.getAllFiles();
    }

   //Get files information by user_id
    @GET
    @Path("/get/{user_id}")
    @RolesAllowed({"user"})
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response getFilesByUserId(@PathParam("user_id") Long user_id) {
        List<FileInfo> fileInfoList = FileInfo.find("user.id", user_id).list();
        if (fileInfoList != null) {
            return Response.ok(fileInfoList).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

   // Get file by fileId
    @GET
    @Path("/get/{user_id}/{fileId}")
    @RolesAllowed({"user"})
    @Transactional
    public Response getFileById(@PathParam("fileId") String fileId, @PathParam("user_id") Long user_id) {
        FileInfo fileInfo = FileInfo.find("fileId", fileId).firstResult();

        if (fileInfo != null) {
            java.nio.file.Path fileLocation = Paths.get(fileInfo.getFileLocation());
            // Do something with fileLocation
            return Response.ok(fileLocation.toFile()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
