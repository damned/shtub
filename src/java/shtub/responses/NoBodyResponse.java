package shtub.responses;

public class NoBodyResponse implements Response {
    private int statusCode;

    public NoBodyResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    public Integer statusCode() {
        return statusCode;
    }
}
