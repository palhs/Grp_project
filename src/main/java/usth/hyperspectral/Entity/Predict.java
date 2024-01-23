package usth.hyperspectral.Entity;

public class Predict {
    private String img_path;
    private String hdr_path;
    private Double x;
    private Double y;

    public Predict(String img_path, String hdr_path, Double x, Double y) {
        this.img_path = img_path;
        this.hdr_path = hdr_path;
        this.x = x;
        this.y = y;
    }

    public Predict() {
    }
    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public String getHdr_path() {
        return hdr_path;
    }

    public void setHdr_path(String hdr_path) {
        this.hdr_path = hdr_path;
    }

    public double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
