package Utilities;


import java.io.Serial;
import java.io.Serializable;


public class Response implements Serializable {
    @Serial
    private static final long serialVersionUID = 52L;
    private String message;
    private int userID;


    public Response(String message, int userID) {
        this.message = message;
        this.userID = userID;

    }
    public Response(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }


    public int getUserID() {
        return userID;
    }

}
