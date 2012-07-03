package shtub;

import javax.servlet.http.HttpServletRequest;

public class Url {
    private HttpServletRequest request;

    public static Url urlFrom(HttpServletRequest request) {
        return new Url(request);
    }

    private Url(HttpServletRequest request) {
        this.request = request;
    }

    public String withoutHost() {
        return compose(request.getRequestURI());
    }

    public String withHost() {
        return compose(request.getRequestURL().toString());
    }

    private String queryParameters() {
        return request.getQueryString();
    }

    private String compose(String baseUri) {
        if (queryParameters() != null) {
            return baseUri + "?" + queryParameters();
        }
        return baseUri;
    }

    @Override
    public String toString() {
        return withHost();
    }
}
