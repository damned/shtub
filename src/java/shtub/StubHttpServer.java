package shtub;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class StubHttpServer {

    private static final String HOSTNAME = "127.0.0.1";

	private static final Logger log = Logger.getLogger(StubHttpServer.class);

    private Server server;
    private List<RequestHandler> handlers = Collections.synchronizedList(new ArrayList<RequestHandler>());
    private final int serverPort;

    public StubHttpServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public StubHttpServer start() {
        server = createServer();
        startJetty();
        waitUntilStarted();
        return this;
    }

    private void waitUntilStarted() {
        while (!server.isStarted()) {
            try {
                Thread.sleep(90);
            } catch (InterruptedException e) {
                log.error(e, "Interrupted waiting for " + serverName());
            }
        }
    }

    public void stop() throws Exception {
        this.server.stop();
        log.debug(serverName() + " stopped: " + server.isStopped());
    }

    private void startJetty() {
        long startTimeMs = currentTimeMillis();
        log.debug("Starting " + serverName() + " ....");
        try {
            server.start();
        } catch (Exception e) {
            throw new ShtubException(e, "Failed to start %s on %d", serverName(), serverPort);
        }

        long startupTime = currentTimeMillis() - startTimeMs;
        log.info(serverName() + " Started in " + startupTime + "ms.");
    }

    private String serverName() {
        return "Stub";
    }

    private Server createServer() {
        Server server = new Server();

        server.setStopAtShutdown(true);
        server.addConnector(createConnector(HOSTNAME, serverPort));
        server.setThreadPool(createThreadPool());
        server.setHandler(createHandler());

        return server;
    }

    private Handler createHandler() {
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.addServlet(new ServletHolder(new HttpServlet() {

            @Override
            public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            	
                String requestUri = null;
				try {
					requestUri = composeQueryUriWithParams(request);
					log.info("Handling get to [" + requestUri + "]");
					for (RequestHandler handler : handlers) {
                        if (handler.handle(requestUri, request, response)) {
					        indicateRequestHandled(request);
					        return;
					    }
					}
				}
                catch (Throwable t) {
					log.error(t, "StubHttpServer blew up for request path '%s'", requestUri);
				}
                handleUnmatchedRequest(response);
            }

            private String composeQueryUriWithParams(HttpServletRequest request) {
                if (request.getQueryString() != null) {
                    return request.getRequestURI() + "?" + request.getQueryString();
                }
                return request.getRequestURI();
            }

            private void indicateRequestHandled(HttpServletRequest request) {
                Request base_request = request instanceof Request ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
                base_request.setHandled(true);
            }
        }
        ), "/");
        return contextHandler;
    }

    private void handleUnmatchedRequest(HttpServletResponse response) {
        response.setStatus(404);
    }

    private Connector createConnector(String hostName, int portNumber) {
        Connector connector = new SelectChannelConnector();
        connector.setPort(portNumber);
        connector.setHost(hostName);
        log.info("Creating " + serverName() + " Jetty Connector at " + hostName + ":" + portNumber);
        return connector;
    }

    private static ThreadPool createThreadPool() {
        QueuedThreadPool threadPool = new QueuedThreadPool(100);
        return threadPool;
    }

    public RequestExpectation expectRequestTo(String path) {
        RequestExpectation expectation = new RequestExpectation();
        expectation.withPath(path);
        addExpectation(expectation);
        return expectation;
    }

    public RequestExpectation matchAnyRequest() {
        RequestExpectation expectation = new RequestExpectation();
        expectation.matchAnyRequest();
        addExpectation(expectation);
        return expectation;
    }

    private void addExpectation(RequestExpectation expectation) {
        handlers.add(expectation);
    }

    public void respondUsing(RequestHandler requestHandler) {
        handlers.clear();
        handlers.add(requestHandler);
    }
}
