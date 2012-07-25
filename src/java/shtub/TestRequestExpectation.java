package shtub;

import shtub.responses.BinaryResponse;
import shtub.responses.NoBodyResponse;
import shtub.responses.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class TestRequestExpectation implements RequestHandler {
    private final Logger log = Logger.getLogger(TestRequestExpectation.class);

    private String expectedPath;

    private boolean matchAnyRequest;

    private List<Parameter> parameters = new ArrayList<Parameter>();

    private Response response;

    public TestRequestExpectation withPath(String path) {
        this.expectedPath = path;
        return this;
    }

    public TestRequestExpectation withParameter(String name, String value) {
        parameters.add(new Parameter(name, value));
        return this;
    }

    public void matchAnyRequest() {
        this.matchAnyRequest = true;
    }

    public void andRespondWith(String responseBody, String mimeType) {
        response = new BinaryResponse(responseBody.getBytes(), mimeType);
    }

    public void andRespondWith(String s) {
        andRespondWith(s, "text/plain");
    }

    public void andRespondWith(byte[] bytes, String mimeType) {
        response = new BinaryResponse(bytes, mimeType);
    }

    public void andRespondWithoutBody(int statusCode) {
        response = new NoBodyResponse(statusCode);
    }

    public boolean handle(Url url, HttpServletRequest request, HttpServletResponse response) throws Exception {
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

    private void respond(HttpServletResponse servletResponse) throws Exception {
        log.info("Responding to request for [" + requestMatchDescription() + "]");

        if (response != null) {
            response.respondVia(servletResponse);
        }
        log.debug("Responded to request");
    }

    private String requestMatchDescription() {
        if (matchAnyRequest) {
            return "matching any request";
        }
        return expectedPath;
    }

    @Override
    public String toString() {
        return "RequestExpectation [expectedPath=" + requestMatchDescription()
                + ", responseRedirectDestination="
                + ", matchAnyRequest=" + matchAnyRequest + "]";
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
