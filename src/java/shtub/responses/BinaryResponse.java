package shtub.responses;

import javax.servlet.http.HttpServletResponse;

public class BinaryResponse implements Response {
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

    public void respondVia(HttpServletResponse servletResponse) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
