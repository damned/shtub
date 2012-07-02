package shtub;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class StubHttpServerIntegrationTest {

    public static final String SERVER_PORT = "1091";
    private StubHttpServer server;
    private HttpClient client;
    private HttpResponse response;

    @Before
    public void runUpStubServer() {
        server = new StubHttpServer(1091);
        server.start();
        client = new HttpClient();
    }

    @After
    public void shutDownStubServer() throws Exception {
        server.stop();
    }

    @Test
    public void gives_canned_text_response_to_any_request() throws Exception {
        server.matchAnyRequest().andRespondWith("Hello, World", "text/plain");

        response = client.get("http://localhost:" + SERVER_PORT + "/whatever");

        assertThat(response.body(), is("Hello, World"));
        assertThat(response.contentType(), is("text/plain"));
        assertThat(response.status(), is(200));
    }

    @Test
    public void gives_specified_response_to_specific_path_request() throws Exception {
        server.expectRequestTo("/correcto").andRespondWith("right path");

        response = client.get("http://localhost:" + SERVER_PORT + "/correcto");

        assertThat(response.body(), is("right path"));
    }

    @Test
    public void gives_404_response_to_specific_path_request() throws Exception {
        server.expectRequestTo("/correcto").andRespondWith("right path");

        response = client.get("http://localhost:1091/in_no_way_correcto");

        assertThat(response.status(), is(404));
        assertThat(response.body(), not("right path"));
    }
}
