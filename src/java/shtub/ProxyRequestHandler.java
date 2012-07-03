package shtub;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ProxyRequestHandler implements RequestHandler {

    private Logger log = Logger.getLogger(ProxyRequestHandler.class);

    public boolean handle(Url requestUrl, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("Proxying request for: %s", requestUrl);
        HttpClient httpClient = new HttpClient();
        HttpResponse destinationServerResponse = null;
        if (request.getMethod().equalsIgnoreCase("get")) {
            httpClient.withHeadersFrom(request);
            destinationServerResponse = httpClient.get(requestUrl.withHost());
        }
        else if (request.getMethod().equalsIgnoreCase("post")) {
            destinationServerResponse = doPost(request, httpClient);
        }
        return destinationServerResponse.copyTo(response);
    }

    private HttpResponse doPost(HttpServletRequest request, HttpClient httpClient) throws Exception {
        HttpResponse destinationServerResponse;
        destinationServerResponse = httpClient.post(request.getRequestURL().toString(), copyParameters(request));
        return destinationServerResponse;
    }

    private Map<String, String> copyParameters(HttpServletRequest request) {
        Enumeration parameterNames = request.getParameterNames();
        Map<String, String> parameters = new HashMap<String, String>();
        while (parameterNames.hasMoreElements()) {
            String name = (String) parameterNames.nextElement();
            parameters.put(name, request.getParameter(name));
        }
        return parameters;
    }
}
