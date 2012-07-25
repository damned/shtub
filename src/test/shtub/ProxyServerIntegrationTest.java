package shtub;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ProxyServerIntegrationTest {

    public static final int DESTINATION_SERVER_PORT = 1091;
    public static final int PROXY_PORT = 1092;
    private StubHttpServer server;
    private HttpClient client;
    private HttpResponse response;
    private HttpProxy proxy;

    @Before
    public void runUpStubServers() {
        server = new StubHttpServer(DESTINATION_SERVER_PORT).start();

        proxy = new HttpProxy(PROXY_PORT).start();

        client = new HttpClient();
    }

    @After
    public void shutDownStubServers() throws Exception {
        server.stop();
        proxy.stop();
    }

    @Test
    public void gives_server_response_to_any_request_via_proxy() throws Exception {
        server.matchAnyRequest().andRespondWith("Hello from the destination server");

        HttpResponse response = requestWithProxy("/whatever");

        assertThat(response.body(), is("Hello from the destination server"));
    }

    @Test
    public void gives_server_response_to_a_binary_request_via_proxy() throws Exception {
        server.matchAnyRequest().andRespondWith(new byte[]{0, 1, 2, 3}, "application/foobar");

        HttpResponse response = requestWithProxy("/whatever");

        assertThat(response.bodyBytes(), is(new byte[]{0, 1, 2, 3}));
        assertThat(response.contentType(), is("application/foobar"));
    }

    @Test
    public void gives_valid_server_response_to_a_cached_request_via_proxy() throws Exception {
        server.matchAnyRequest().andRespondWithoutBody(302);

        HttpResponse response = requestWithProxy("/whatever");

        assertThat(response.bodyBytes(), is(new byte[0]));
        assertThat(response.status(), is(302));
    }

    @Test
    public void gives_valid_server_response_to_a_proxy_request() throws Exception {
        server.expectRequestTo("/posturl")
                .withParameter("a", "1")
                .withParameter("b", "2")
                .andRespondWith("WOOT!");


        Map<String, String> params = new HashMap<String, String>();
        params.put("a", "1");
        params.put("b", "2");

        client.useProxy("localhost", PROXY_PORT);
        response = client.post(destinationUrl("/posturl"), params);

        assertThat(response.body(), is("WOOT!"));
        assertThat(response.status(), is(200));
    }

    @Test
    public void passes_on_get_query_parameters_to_proxied_server() throws Exception {
        String uriWithParams = "/get?foo=bar&sna=fu";

        server.expectQueryTo(uriWithParams).andRespondWith("all the info");

        response = requestWithProxy(uriWithParams);

        assertThat(response.status(), is(200));
        assertThat(response.body(), is("all the info"));
    }

    private String destinationUrl(String path) {
        return "http://localhost:" + DESTINATION_SERVER_PORT + path;
    }

    private HttpResponse requestWithProxy(String path) throws Exception {
        client.useProxy("localhost", PROXY_PORT);
        return client.get(destinationUrl(path));
    }
}
