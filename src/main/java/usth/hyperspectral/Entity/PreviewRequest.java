package usth.hyperspectral.Entity;

public class PreviewRequest {
    private String fileNameHDR;
    private String fileNameIMG;


    public PreviewRequest() {
    }

    public PreviewRequest(String fileNameHDR, String fileNameIMG) {
        this.fileNameHDR = fileNameHDR;
        this.fileNameIMG = fileNameIMG;
    }

    public String getFileNameHDR() {
        return fileNameHDR;
    }

    public void setFileNameHDR(String fileNameHDR) {
        this.fileNameHDR = fileNameHDR;
    }

    public String getFileNameIMG() {
        return fileNameIMG;
    }

    public void setFileNameIMG(String fileNameIMG) {
        this.fileNameIMG = fileNameIMG;
    }
}
