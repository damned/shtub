package shtub.requests;

import shtub.Url;

import javax.servlet.http.HttpServletRequest;

public class PathAndParametersMatcher implements RequestMatcher {
    private String uriWithParams;

    public PathAndParametersMatcher(String uriWithParams) {
        this.uriWithParams = uriWithParams;
    }

    public boolean matches(HttpServletRequest request) {
        return uriWithParams.equals(Url.urlFrom(request).withoutHost());
    }
}
