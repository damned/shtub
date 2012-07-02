package shtub;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProxyRequestHandler implements RequestHandler {

    private Logger log = Logger.getLogger(ProxyRequestHandler.class);

    public boolean handle(String requestUriWithParams, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("Handling request for: %s", request.getRequestURL());
        HttpClient httpClient = new HttpClient();
        httpClient.withHeadersFrom(request);
        HttpResponse destinationServerResponse = httpClient.get(request.getRequestURL().toString());
        return destinationServerResponse.copyTo(response);
    }
}
