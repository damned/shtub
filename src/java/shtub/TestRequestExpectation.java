package shtub;

import org.apache.commons.io.IOUtils;
import shtub.responses.BinaryResponse;
import shtub.responses.NoBodyResponse;
import shtub.responses.Response;

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

    private boolean matchAnyRequest;
    private int statusCode = 200;

    private List<Parameter> parameters = new ArrayList<Parameter>();
    private BinaryResponse binaryResponse;
    private Response response;

    public void matchAnyRequest() {
        this.matchAnyRequest = true;
    }

    public TestRequestExpectation withPath(String path) {
        this.expectedPath = path;
        return this;
    }

    public void andRespondWith(String responseBody, String mimeType) {
        binaryResponse = new BinaryResponse(responseBody.getBytes(), mimeType);
    }

    public boolean handle(Url url, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (requestMatchesExpectation(url.withoutHost(), request)) {
            respond(response);
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

    private void respond(HttpServletResponse servletResponse) throws IOException {
        log.info("Responding to request for [" + requestMatchDescription() + "]");

        if (response != null) {
            response.respondVia(servletResponse);
        }
        else if (responseBodyBytes() != null) {
            log.debug("Responding with '%d' and body '%s'", statusCode, new String(responseBodyBytes()));
            servletResponse.setStatus(statusCode);
            copyContentsToResponse(servletResponse, new ByteArrayInputStream(responseBodyBytes()), binaryResponse.mimeType());
        }
        log.debug("Responded to request");
    }

    private byte[] responseBodyBytes() {
        return binaryResponse.bytes();
    }

    private String requestMatchDescription() {
        if (matchAnyRequest) {
            return "matching any request";
        }
        return expectedPath;
    }

    private void copyContentsToResponse(HttpServletResponse response, InputStream responseData, String responseMimeType) throws IOException {
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
                + ", binaryResponse=" + binaryResponse
                + ", responseRedirectDestination="
                + ", matchAnyRequest=" + matchAnyRequest
                + ", statusCode=" + statusCode + "]";
    }


    public void andRespondWith(String s) {
        andRespondWith(s, "text/plain");
    }

    public void andRespondWith(byte[] bytes, String mimeType) {
        binaryResponse = new BinaryResponse(bytes, mimeType);
    }

    public void andRespondWithoutBody(int statusCode) {
        response = new NoBodyResponse(statusCode);
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
