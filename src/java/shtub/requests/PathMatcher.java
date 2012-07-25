package shtub.requests;

import shtub.Url;

import javax.servlet.http.HttpServletRequest;

public class PathMatcher implements RequestMatcher {
    private String path;

    public PathMatcher(String path) {
        this.path = path;
    }

    public boolean matches(HttpServletRequest request) {
        return path.equals(Url.urlFrom(request).withoutHostOrQueryString());
    }
}
