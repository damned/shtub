package shtub.responses;

import org.apache.commons.io.IOUtils;
import shtub.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BinaryResponse implements Response {
    private final Logger log = Logger.getLogger(BinaryResponse.class);
    private byte[] bytes;
    private String mimeType;
    private int statusCode = 200;

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

    public void respondVia(HttpServletResponse servletResponse) throws Exception {
        byte[] bytes = bytes();
        log.debug("Responding with '%d' and body '%s'", statusCode, new String(bytes));
        servletResponse.setStatus(statusCode);
        copyContentsToResponse(servletResponse, new ByteArrayInputStream(bytes), mimeType(), statusCode);
    }

    public void copyContentsToResponse(HttpServletResponse response, InputStream responseData, String responseMimeType, int statusCode) throws IOException {
        if (responseMimeType != null) {
            response.setContentType(responseMimeType);
        }
        try {
            IOUtils.copy(responseData, response.getOutputStream());
            response.setStatus(statusCode);
        }
        finally {
            IOUtils.closeQuietly(responseData);
        }
    }
}
