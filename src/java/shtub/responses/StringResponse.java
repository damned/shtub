package shtub.responses;

import javax.servlet.http.HttpServletResponse;

public class StringResponse implements Response {

    private Response response;

    public StringResponse(String body, String mimeType) {
        response = new BinaryResponse(body.getBytes(), mimeType);
    }

    public void respondVia(HttpServletResponse servletResponse) throws Exception {
        response.respondVia(servletResponse);
    }
}
