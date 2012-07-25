package shtub.responses;

import javax.servlet.http.HttpServletResponse;

public interface Response {
    void respondVia(HttpServletResponse servletResponse) throws Exception;
}
