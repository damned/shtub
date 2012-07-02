package shtub;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

        client.useProxy("localhost", PROXY_PORT);
        response = client.get("http://localhost:" + DESTINATION_SERVER_PORT + "/whatever");

        assertThat(response.body(), is("Hello from the destination server"));
    }

    @Test
    public void gives_server_response_to_a_binary_request_via_proxy() throws Exception {
        server.matchAnyRequest().andRespondWith(new byte[]{0, 1, 2, 3}, "application/foobar");

        client.useProxy("localhost", PROXY_PORT);
        response = client.get("http://localhost:" + DESTINATION_SERVER_PORT + "/whatever");

        assertThat(response.bodyBytes(), is(new byte[]{0, 1, 2, 3}));
        assertThat(response.contentType(), is("application/foobar"));
    }
}
