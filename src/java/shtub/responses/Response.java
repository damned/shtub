package shtub.responses;

import javax.servlet.http.HttpServletResponse;

public interface Response {

    void respondVia(HttpServletResponse servletResponse) throws Exception;

    public static Response NULL = new Response() {
        public void respondVia(HttpServletResponse servletResponse) throws Exception {
            // do nowt
        }
    };
}
