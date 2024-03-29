package usth.hyperspectral.Controller;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import usth.hyperspectral.Entity.*;
import usth.hyperspectral.resource.PredictResource;
import usth.hyperspectral.resource.PreviewResource;
import usth.hyperspectral.service.FileUploadService;

import java.nio.file.Paths;
import java.util.List;

@Path("/file")
public class UploadController {
    @Inject
    FileUploadService fileUploadService;

    @Inject
    PreviewResource previewResource;

    @Inject
    PredictResource predictResource;



    @POST
    @RolesAllowed({"user"})
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Response fileUpload(@MultipartForm MultipartFormDataInput input) {
        try{
            Response imgUploadResponse = fileUploadService.uploadImg(input);
            if (imgUploadResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return imgUploadResponse;
            }
            String imgUploadResult = (String) imgUploadResponse.getEntity();

            Response hdrUploadResponse = fileUploadService.uploadHDR(input);
            if (hdrUploadResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                return hdrUploadResponse;
            }
            String hdrUploadResult = (String) hdrUploadResponse.getEntity();

            return Response.ok().entity(imgUploadResult + "\n" + hdrUploadResult).build();
        }catch (Exception e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing the request: " + e.getMessage())
                    .build();
        }
    }

//    @POST
//    @RolesAllowed({"user"})
//    @Path("/upload")
//    //    @PermitAll
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response fileUpload(@MultipartForm MultipartFormDataInput input) {
//        String imgUploadResult = fileUploadService.uploadImg(input);
//        String hdrUploadResult = fileUploadService.uploadHDR(input);
//        return Response.ok().entity(imgUploadResult + "\n" + hdrUploadResult).build();
//    }


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
    @Path("/get/u")
    @RolesAllowed({"user"})
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response getFilesByUserId(@Context SecurityContext securityContext) {
        Long user_id = Long.parseLong(securityContext.getUserPrincipal().getName());
        List<FileInfo> fileInfoList = FileInfo.find("user.id", user_id).list();
        if (fileInfoList != null) {
            return Response.ok(fileInfoList).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    // Get file by fileId
    @GET
    @Path("/get/u/{fileId}")
    @RolesAllowed({"user"})
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Transactional
    public Response getFileById(@PathParam("fileId") String fileId, @Context SecurityContext securityContext) {
        FileInfo fileInfo = FileInfo.find("fileId", fileId).firstResult();
        Long user_id = Long.parseLong(securityContext.getUserPrincipal().getName());
        if (fileInfo != null && fileInfo.getUser().getUser_id().equals(user_id)) {
            java.nio.file.Path fileLocation = Paths.get(fileInfo.getFileLocation());
            // Do something with fileLocation
            return Response.ok(fileLocation.toFile()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    // Delete file by fileId
    @DELETE
    @Path("/delete/u/{fileId}")
    @RolesAllowed({"admin","user"})
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFileById(@PathParam("fileId") String fileId, @Context SecurityContext securityContext) {
        FileInfo fileInfo = FileInfo.find("fileId", fileId).firstResult();
        Long user_id = Long.parseLong(securityContext.getUserPrincipal().getName());

        if (fileInfo != null && fileInfo.getUser().getUser_id().equals(user_id)) {
            boolean isDeleted = fileUploadService.DeleteFileById(fileId);
            if (isDeleted) {
                return Response.ok()
                        .entity("File with id " + fileId + " deleted successfully")
                        .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @RolesAllowed({"user"})
    @Path("/preview")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Transactional
    public Response addPreview(PreviewRequest previewRequest) {
        if(previewRequest == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("The value of the body is missing").build();
        }

        FileInfo fileInfoHDR = FileInfo.find("fileName", previewRequest.getFileNameHDR()).firstResult();
        FileInfo fileInfoIMG = FileInfo.find("fileName", previewRequest.getFileNameIMG()).firstResult();

        if(fileInfoIMG == null || fileInfoHDR == null){
            return Response.status(Response.Status.NOT_FOUND).entity("Can not find fileInfo").build();
        }

        String hdr_path = fileInfoHDR.getFileLocation();
        String img_path = fileInfoIMG.getFileLocation();

        if(hdr_path == null || img_path == null){
            return Response.status(Response.Status.NOT_FOUND).entity("Can not find fileLocation").build();
        }

        Preview preview = new Preview(img_path,hdr_path);

        return previewResource.addPreview(preview);
    }
    @POST
    @RolesAllowed({"user"})
    @Path("/predict")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Transactional
    public Response addPredict(PredictRequest predictRequest) {
        if(predictRequest == null){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        FileInfo fileInfoHDR = FileInfo.find("fileName", predictRequest.getFileNameHDR()).firstResult();
        FileInfo fileInfoIMG = FileInfo.find("fileName", predictRequest.getFileNameIMG()).firstResult();

        if (fileInfoIMG == null || fileInfoHDR == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Double x = predictRequest.getX();
        Double y = predictRequest.getY();

        String hdr_path = fileInfoHDR.getFileLocation();
        String img_path = fileInfoIMG.getFileLocation();

        Predict predict = new Predict(img_path, hdr_path, x , y);

        return predictResource.addPredict(predict);
    }
}