package shtub.requests;

import javax.servlet.http.HttpServletRequest;

public class NoRequestMatcher implements RequestMatcher {
    public boolean matches(HttpServletRequest request) {
        return false;
    }
}
