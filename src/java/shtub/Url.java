package shtub;

import javax.servlet.http.HttpServletRequest;

public class Url {
    private String queryString;
    private String requestUri;
    private String requestUrl;

    public Url(String uriWithParams) {
        String[] pathAndParams = uriWithParams.split("\\?");
        requestUrl = requestUri = pathAndParams[0];
        queryString = pathAndParams[1];
    }

    public static Url urlFrom(HttpServletRequest request) {
        return new Url(request);
    }

    public static Url fromPathAndParams(String uriWithParams) {
        return new Url(uriWithParams);
    }

    private Url(HttpServletRequest request) {
        requestUrl = request.getRequestURL().toString();
        requestUri = request.getRequestURI();
        queryString = request.getQueryString();
    }

    public String withoutHost() {
        return compose(withoutHostOrQueryString());
    }

    public String withoutHostOrQueryString() {
        return requestUri;
    }

    public String withHost() {
        return compose(requestUrl);
    }

    private String queryParameters() {
        return queryString;
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
