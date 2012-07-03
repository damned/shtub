package shtub;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TestRequestExpectation implements RequestHandler {
    private final Logger log = Logger.getLogger(TestRequestExpectation.class);

    private String expectedPath;
    private String responseMimeType;

    private boolean matchAnyRequest;
    private int statusCode = 200;
    private byte[] responseBodyBytes;

    private List<Parameter> parameters = new ArrayList<Parameter>();

    public void matchAnyRequest() {
        this.matchAnyRequest = true;
    }

    public TestRequestExpectation withPath(String path) {
        this.expectedPath = path;
        return this;
    }

    public void andRespondWith(String responseBody, String mimeType) {
        this.responseBodyBytes = responseBody.getBytes();
        this.responseMimeType = mimeType;
    }

    public boolean handle(Url url, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (requestMatchesExpectation(url.withoutHost(), request)) {
            respond(response, this);
            return true;
        }
        return false;
    }

    private boolean requestMatchesExpectation(String requestUriWithParams, HttpServletRequest request) {
        return matchAnyRequest || (requestUriWithParams.equals(expectedPath) && parametersMatch(request));
    }

    private boolean parametersMatch(HttpServletRequest request) {
        for (Parameter parameter : parameters) {
            if (! parameter.isIn(request)) {
                return false;
            }
        }
        return true;
    }

    private void respond(HttpServletResponse response, TestRequestExpectation requestExpectation) throws IOException {
        log.info("Responding to request for [" + requestMatchDescription() + "]");

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

    public void andRespondWithoutBody(int statusCode) {
        responseBodyBytes = new byte[0];
        this.statusCode = statusCode;
    }

    public TestRequestExpectation withParameter(String name, String value) {
        parameters.add(new Parameter(name, value));
        return this;
    }

    private class Parameter {
        private String name;
        private String value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public boolean isIn(HttpServletRequest request) {
            String parameter = request.getParameter(name);
            if (value == null) {
                return parameter == null;
            }
            return value.equals(parameter);
        }
    }
}
