package shtub;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class HttpClient {
    private List<Header> headers = new ArrayList<Header>();
    private String proxyHost;
    private int proxyPort;

    public HttpResponse get(String url) throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();
        applyProxy(client);
        doNotFollowRedirects(client);

        HttpGet request = new HttpGet(url);
        for (Header header : headers) {
            request.addHeader(header);
        }

        org.apache.http.HttpResponse response = client.execute(request);
        return new HttpResponse(response);
    }

    private void doNotFollowRedirects(DefaultHttpClient client) {
        client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
    }

    private void applyProxy(DefaultHttpClient client) {
        if (proxyHost != null) {
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(proxyHost, proxyPort));
        }
    }

    public void useProxy(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public HttpClient withHeadersFrom(HttpServletRequest request) {
        Enumeration headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = (String)headerNames.nextElement();
            headers.add(new BasicHeader(headerName, request.getHeader(headerName)));
        }
        return this;
    }
}
