package gr.ntua.metal.gaiopepper.models;

public class MessageItem {
    public static final int LayoutUser = 0;
    public static final int LayoutRobot = 1;
    public static final int LayoutRobotImage = 2;
    public static final int LayoutUserImage = 3;


    private int viewType;
    private int image;
    private String message;
    private int imageMessage;

    public MessageItem(int viewType, int image, String message) {
        this.viewType = viewType;
        this.image = image;
        this.message = message;
    }

    public MessageItem(int viewType, int image, int imageMessage) {
        this.viewType = viewType;
        this.image = image;
        this.imageMessage = imageMessage;
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

    public int getImageMessage() {
        return imageMessage;
    }

    public void setImageMessage(int imageMessage) {
        this.imageMessage = imageMessage;
    }

    public int getViewType() {
        return viewType;
    }
}
