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

    private List<RequestMatcher> matchers = new ArrayList<RequestMatcher>();

    private Response response = Response.NULL;

    public TestRequestExpectation withPath(String path) {
        this.matchers.add(new PathMatcher(path));
        return this;
    }

    public TestRequestExpectation withPathAndQuery(String uriWithParams) {
        this.matchers.add(new PathAndParametersMatcher(uriWithParams));
        return this;
    }

    public TestRequestExpectation withParameter(String name, String value) {
        matchers.add(new ParameterMatcher(name, value));
        return this;
    }

    public void matchAnyRequest() {
        matchers.add(new AnyRequestMatcher());
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
        if (matches(request, matchers)) {
            respond(response);
            return true;
        }
        return false;
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
        if (matchers.isEmpty()) {
            return "matching no requests";
        }
        return description(matchers);
    }

    private String description(List<RequestMatcher> matchers) {
        StringBuilder all = new StringBuilder();
        for (RequestMatcher matcher : matchers) {
            all.append(matcher.toString());
            all.append("   ");
        }
        return all.toString();
    }

    @Override
    public String toString() {
        return "RequestExpectation [matching=" + requestMatchDescription() + "]";
    }
}
