package gr.ntua.metal.gaiopepper.models;

public class MessageItem {

    private int image;
    private String message;

    public MessageItem(int image, String message) {
        this.image = image;
        this.message = message;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
