package shtub;

import shtub.requests.*;
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

    private List<RequestMatcher> matchers = new ArrayList<RequestMatcher>();

    private RequestMatcher matcher = new NoRequestMatcher();

    private Response response = Response.NULL;

    public TestRequestExpectation withPath(String path) {
        this.matchers.add(new PathMatcher(path));
        return this;
    }

    public void withPathAndQuery(String uriWithParams) {
        this.matchers.add(new PathAndParametersMatcher(uriWithParams));
    }

    public TestRequestExpectation withParameter(String name, String value) {
        matchers.add(new ParameterMatcher(name, value));
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
        if (requestMatchesExpectation(request)) {
            respond(response);
            return true;
        }
        return false;
    }

    private boolean requestMatchesExpectation(HttpServletRequest request) {
        return matcher.matches(request) || matches(request);
    }

    private boolean matches(HttpServletRequest request) {
        return matches(request, matchers);
    }

    private boolean matches(HttpServletRequest request, List<RequestMatcher> matchers) {
        for (RequestMatcher matcher : matchers) {
            if (!matcher.matches(request)) {
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
}
