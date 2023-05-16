package database;

public class Response {
    public final Status status;
    public final String message;

    public Response(Status status, String message) {
        this.status = status;
        this.message = message;
    }
}
