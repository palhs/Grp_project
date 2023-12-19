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
import usth.hyperspectral.resource.FileInfo;
import usth.hyperspectral.service.FileUploadService;

import java.nio.file.Paths;
import java.util.List;

@Path("/file")
public class UploadController {
    @Inject
    FileUploadService fileUploadService;

    @POST
//    @PermitAll
    @RolesAllowed({"user"})
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response fileUpload(@MultipartForm MultipartFormDataInput input) {
        return Response.ok().entity(fileUploadService.uploadFile(input)).build();
    }

    //Get all files

    @GET
//    @RolesAllowed({"user"})
    @Path("/get")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public List<FileInfo> getAllFiles() {
        return fileUploadService.getAllFiles();
    }

    @GET
    @Path("/{id}")
    @PermitAll
//    @RolesAllowed({"user"})
    @Transactional
    public Response getFileById(@PathParam("id") String fileId) {
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
