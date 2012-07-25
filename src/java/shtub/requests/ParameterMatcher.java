package shtub.requests;

import javax.servlet.http.HttpServletRequest;

public class ParameterMatcher implements RequestMatcher {
    private String name;
    private String value;

    public ParameterMatcher(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public boolean isIn(HttpServletRequest request) {
        return matches(request);
    }

    public boolean matches(HttpServletRequest request) {
        String parameter = request.getParameter(name);
        if (value == null) {
            return parameter == null;
        }
        return value.equals(parameter);
    }
}
