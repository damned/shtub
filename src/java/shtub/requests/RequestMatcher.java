package shtub.requests;

import javax.servlet.http.HttpServletRequest;

public interface RequestMatcher {
    boolean matches(HttpServletRequest request);
}
