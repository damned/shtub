package shtub;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

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

    public HttpResponse post(String url, Map<String,String> params) throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();

        applyProxy(client);
        doNotFollowRedirects(client);

        HttpPost request = new HttpPost(url);

        for (Header header : headers) {
            request.addHeader(header);
        }

        List<NameValuePair> httpParameters = new ArrayList<NameValuePair>();
        for(String key : params.keySet()) {
            httpParameters.add(new BasicNameValuePair(key, params.get(key)));
        }
        request.setEntity(new UrlEncodedFormEntity(httpParameters, HTTP.UTF_8));

        org.apache.http.HttpResponse response = client.execute(request);

        return new HttpResponse(response);
    }
}
