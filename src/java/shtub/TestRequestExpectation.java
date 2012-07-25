package shtub;

import shtub.requests.AnyRequestMatcher;
import shtub.requests.NoRequestMatcher;
import shtub.requests.RequestMatcher;
import shtub.responses.BinaryResponse;
import shtub.responses.NoBodyResponse;
import shtub.responses.Response;
import shtub.responses.StringResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class TestRequestExpectation implements RequestHandler {
    private final Logger log = Logger.getLogger(TestRequestExpectation.class);

    private String expectedPath;

    private List<Parameter> parameters = new ArrayList<Parameter>();

    private RequestMatcher matcher = new NoRequestMatcher();

    private Response response = Response.NULL;

    public TestRequestExpectation withPath(String path) {
        this.expectedPath = path;
        return this;
    }

    public TestRequestExpectation withParameter(String name, String value) {
        parameters.add(new Parameter(name, value));
        return this;
    }

    public void matchAnyRequest() {
        matcher = new AnyRequestMatcher();
    }

    public void andRespondWith(String responseBody, String mimeType) {
        response = new StringResponse(responseBody, mimeType);
    }

    public void andRespondWith(String s) {
        response = new StringResponse(s, "text/plain");
    }

    public void andRespondWith(byte[] bytes, String mimeType) {
        response = new BinaryResponse(bytes, mimeType);
    }

    public void andRespondWithoutBody(int statusCode) {
        response = new NoBodyResponse(statusCode);
    }

    public boolean handle(Url url, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (requestMatchesExpectation(url, request)) {
            respond(response);
            return true;
        }
        return false;
    }

    private boolean requestMatchesExpectation(Url url, HttpServletRequest request) {
        return matcher.matches(request) || (url.withoutHostOrQueryString().equals(expectedPath) && parametersMatch(request));
    }

    private boolean parametersMatch(HttpServletRequest request) {
        List<Parameter> parametersToMatch = parameters;
        return matches(request, parametersToMatch);
    }

    private boolean matches(HttpServletRequest request, List<Parameter> parametersToMatch) {
        for (Parameter parameter : parametersToMatch) {
            if (! parameter.isIn(request)) {
                return false;
            }
        }
        return true;
    }

    private void respond(HttpServletResponse servletResponse) throws Exception {
        log.info("Responding to request for [" + requestMatchDescription() + "]");
        response.respondVia(servletResponse);
        log.debug("Responded to request");
    }

    private String requestMatchDescription() {
        if (matcher.matches(null)) {
            return "matching any request";
        }
        return expectedPath;
    }

    @Override
    public String toString() {
        return "RequestExpectation [expectedPath=" + requestMatchDescription()
                + ", responseRedirectDestination="
                + ", matchAnyRequest=" + matcher.matches(null) + "]";
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
