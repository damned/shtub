package shtub.requests;

import javax.servlet.http.HttpServletRequest;

public class AnyRequestMatcher implements RequestMatcher {
    public boolean matches(HttpServletRequest request) {
        return true;
    }
}
