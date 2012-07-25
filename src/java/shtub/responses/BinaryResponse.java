package shtub.responses;

public class BinaryResponse {
    private byte[] bytes;
    private String mimeType;

    public BinaryResponse(byte[] bytes, String mimeType) {
        this.bytes = bytes;
        this.mimeType = mimeType;
    }

    public byte[] bytes() {
        return bytes;
    }

    public String mimeType() {
        return mimeType;
    }
}
