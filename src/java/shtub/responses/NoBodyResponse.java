package shtub.responses;

import shtub.Logger;

import javax.servlet.http.HttpServletResponse;

public class NoBodyResponse implements Response {
    private final Logger log = Logger.getLogger(NoBodyResponse.class);

    private int statusCode;

    public NoBodyResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    public Integer statusCode() {
        return statusCode;
    }

    public void respondVia(HttpServletResponse servletResponse) {
        log.debug("Responding with '%d' and no body", statusCode());
        servletResponse.setStatus(statusCode());
    }
}
