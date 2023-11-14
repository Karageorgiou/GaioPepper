package gr.ntua.metal.gaiopepper.models;

public class MessageItem {
    public static final int LayoutUser = 0;
    public static final int LayoutRobot = 1;


    private int viewType;
    private int image;
    private String message;

    public MessageItem(int viewType, int image, String message) {
        this.viewType = viewType;
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

    public int getViewType() { return viewType; }
}
