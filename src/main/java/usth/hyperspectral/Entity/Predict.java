package usth.hyperspectral.Entity;

public class Predict {
    private String img_path;
    private String hdr_path;
    private String x;
    private String y;

    public Predict(String img_path, String hdr_path, String x, String y) {
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

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
