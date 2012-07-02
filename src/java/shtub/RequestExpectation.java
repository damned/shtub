package shtub;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RequestExpectation implements RequestHandler {
    private final Logger log = Logger.getLogger(RequestExpectation.class);

    private String expectedPath;
    private String responseMimeType;
    private boolean matchAnyRequest;
    private int statusCode = 200;
    private byte[] responseBodyBytes;

    public void matchAnyRequest() {
        this.matchAnyRequest = true;
    }

    public RequestExpectation withPath(String path) {
        this.expectedPath = path;
        return this;
    }

    public void andRespondWith(String responseBody, String mimeType) {
        this.responseBodyBytes = responseBody.getBytes();
        this.responseMimeType = mimeType;
    }

    public boolean handle(String requestUriWithParams, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (requestMatchesExpectation(requestUriWithParams)) {
            respond(response, this);
            return true;
        }
        return false;
    }

    private boolean requestMatchesExpectation(String requestUriWithParams) {
        return matchAnyRequest || requestUriWithParams.matches(requestMatchDescription());
    }

    private void respond(HttpServletResponse response, RequestExpectation requestExpectation) throws IOException {
        log.info("Responding to get for [" + requestMatchDescription() + "]");

        if (requestExpectation.responseBodyBytes != null) {
            log.debug("Responding with '%d' and body '%s'", statusCode, new String(responseBodyBytes));
            response.setStatus(statusCode);
            copyContentsToResponse(response, new ByteArrayInputStream(responseBodyBytes));
        }
        log.debug("Responded to request");
    }

    private String requestMatchDescription() {
        if (matchAnyRequest) {
            return "matching any request";
        }
        return expectedPath;
    }

    private void copyContentsToResponse(HttpServletResponse response, InputStream responseData) throws IOException {
        response.setContentType(responseMimeType);
        try {
            IOUtils.copy(responseData, response.getOutputStream());
            response.setStatus(statusCode);
        }
        finally {
            IOUtils.closeQuietly(responseData);
        }
    }

    @Override
    public String toString() {
        return "RequestExpectation [expectedPath=" + requestMatchDescription()
                + ", responseMimeType=" + responseMimeType
                + ", responseBody=" + new String(responseBodyBytes)
                + ", responseRedirectDestination="
                + ", matchAnyRequest=" + matchAnyRequest
                + ", statusCode=" + statusCode + "]";
    }


    public void andRespondWith(String s) {
        andRespondWith(s, "text/plain");
    }

    public void andRespondWith(byte[] bytes, String mimeType) {
        responseBodyBytes = bytes;
        responseMimeType = mimeType;
    }
}
