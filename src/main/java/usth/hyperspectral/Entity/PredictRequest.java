package usth.hyperspectral.Entity;

public class PredictRequest {
    private String fileNameHDR;
    private String fileNameIMG;
    private Double x;
    private Double y;

    public PredictRequest(String fileNameHDR, String fileNameIMG, Double x, Double y) {
        this.fileNameHDR = fileNameHDR;
        this.fileNameIMG = fileNameIMG;
        this.x = x;
        this.y = y;
    }

    public PredictRequest() {
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

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
