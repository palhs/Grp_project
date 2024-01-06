package usth.hyperspectral.Entity;

public class Preview {
    private String img_path;
    private String hdr_path;

    // Default constructor
    public Preview() {
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

    public Preview(String img_path, String hdr_path) {
        this.img_path = img_path;
        this.hdr_path = hdr_path;
    }
}
