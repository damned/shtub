package shtub;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
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
import java.io.*;
import java.util.*;

import java.util.concurrent.ExecutorService;

public class StubHttpServer {

    private static final String HOSTNAME = "127.0.0.1";

	private static final Logger log = Logger.getLogger(StubHttpServer.class);

    private Server server;
    private List<RequestExpectation> expectations = Collections.synchronizedList(new ArrayList<RequestExpectation>());
    private String stubServerName;
    private final int serverPort;

    public StubHttpServer(String serverName, int serverPort) {
        this.serverPort = serverPort;
        setStubServerName(serverName);
    }

    public void start() {
        server = createServer();
        startJetty();
        waitUntilStarted();
    }

    private void waitUntilStarted() {
        while (!server.isStarted()) {
            try {
                Thread.sleep(90);
            } catch (InterruptedException e) {
                log.error(e, "Interrupted waiting for " + getServerDescription());
            }
        }
    }

    public void stop() throws Exception {
        this.server.stop();
        log.debug(getServerDescription() + " stopped: " + server.isStopped());
    }

    private void startJetty() {
        long startTimeMs = System.currentTimeMillis();
        log.debug("Starting " + getServerDescription() + " ....");
        try {
            server.start();
        } catch (Exception e) {
            throw new ShtubException(e, "Failed to start %s on %d", getServerDescription(), serverPort);
        }

        long startupTime = System.currentTimeMillis() - startTimeMs;
        log.info(getServerDescription() + " Started in " + startupTime + "ms.");
    }

    public String hostAndPort() {
        return HOSTNAME + ":" + serverPort;
    }

    private String getServerDescription() {
        return "Stub " + stubServerName;
    }

    private void setStubServerName(String serverName) {
        this.stubServerName = serverName;
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
					Map<String, String> requestHeaders = getRequestHeaders(request);
                    String requestBody = getRequestBody(request);
					for (RequestExpectation expectation : expectations) {
                        if (expectation.handle(response, requestUri, requestHeaders, requestBody)) {
					        indicateRequestHandled(request);
					        return;
					    }
					}
				}
                catch (Throwable t) {
					log.error(t, "StubHttpServer blew up for request path '%s'", requestUri);
				}
                throw new UnhandledRequestException(requestUri, expectations);
                
            }

            private String getRequestBody(HttpServletRequest request) throws IOException {
                ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream();
                IOUtils.copy(request.getInputStream(), bodyBytes);
                return new String(bodyBytes.toByteArray(), "UTF-8");
            }

            private Map<String, String> getRequestHeaders(HttpServletRequest request) {
                Map<String, String> requestHeaders = new HashMap<String, String>();
                Enumeration<?> namesEnumerator = request.getHeaderNames();
                String headerName;
                while (namesEnumerator.hasMoreElements()) {
                    headerName = (String) namesEnumerator.nextElement();
                    requestHeaders.put(headerName, request.getHeader(headerName));
                }
                return requestHeaders;
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

    private Connector createConnector(String hostName, int portNumber) {
        Connector connector = new SelectChannelConnector();
        connector.setPort(portNumber);
        connector.setHost(hostName);
        log.info("Creating " + getServerDescription() + " Jetty Connector at " + hostName + ":" + portNumber);
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

    public void resetExpectations() {
        expectations.clear();
    }

    public boolean matchedAllExpectations() {
        for (RequestExpectation expectation : expectations) {
            if (! expectation.isMatched()) {
                return false;
            }
        }
        return true;
    }

    private void addExpectation(RequestExpectation expectation) {
        expectations.add(expectation);
    }
    

    public class RequestExpectation {

        private String expectedPath;
        private int millisecondsToBlock;
        private String responseMimeType;
        private String responseFilename;
        private String responseBody;
        private String responseRedirectDestination;
        private boolean matchAnyRequest;
        private boolean matched;
        private String expectedHeaderName;
        private String expectedHeaderValue;
        private List<BodyMatcher> requestBodyMatcher;
        private int statusCode = 200;

        public void matchAnyRequest() {
            this.matchAnyRequest = true;
        }

        public RequestExpectation withPath(String path) {
            this.expectedPath = path;
            return this;
        }

        public RequestExpectation withHeader(String headerName, String headerValue) {
            this.expectedHeaderName = headerName;
            this.expectedHeaderValue = headerValue;
            return this;
        }

        public RequestExpectation withRequestBodyMatching(BodyMatcher... requestBodyMatcher) {
            this.requestBodyMatcher = Arrays.asList(requestBodyMatcher);
            return this;
        }

        public void andRespondWith(String responseBody, String mimeType) {
            this.responseBody = responseBody;
            this.responseMimeType = mimeType;
        }

        public void andRespondWithFile(String filename, String mimeType) {
            this.responseFilename = filename;
            this.responseMimeType = mimeType;
        }

        public void andRespondWithRedirectTo(String redirectDestination) {
            this.responseRedirectDestination=redirectDestination;
        }

        public RequestExpectation andBlockForSeconds(int secondsToBlock) {
            this.millisecondsToBlock = secondsToBlock * 1000;
            return this;
        }

		public RequestExpectation andBlockForMilliseconds(int millisecondsToBlock) {
			this.millisecondsToBlock = millisecondsToBlock;
			return this;
		}

		public boolean handle(HttpServletResponse response, String requestUriWithParams, Map<String, String> requestHeaders, String requestBody) throws IOException {
            if (requestMatchesExpectation(requestUriWithParams, requestHeaders, requestBody)) {
                respond(response, this);
                matched = true;
                return true;
            }
            return false;
        }

        private boolean requestMatchesExpectation(String requestUriWithParams, Map<String, String> requestHeaders, String requestBody) {
            return matchAnyRequest || matchesSpecificRequest(requestUriWithParams, requestHeaders, requestBody);
        }

        private boolean matchesSpecificRequest(String requestUriWithParams, Map<String, String> requestHeaders, String requestBody) {
            return requestUriWithParams.matches(requestMatchDescription())
            && matchesExpectedHeaders(requestHeaders)
            && requestBodyMatches(requestBody);
        }

        private boolean requestBodyMatches(String requestBody) {
            if (requestBodyMatcher == null) {
                return true;
            }
            for(BodyMatcher bodyMatcher:requestBodyMatcher){
              boolean isMatch = bodyMatcher.matches(requestBody);
                if(!isMatch){
                    return isMatch;
                }
            }
            return true;
        }

        private boolean matchesExpectedHeaders(Map<String, String> requestHeaders) {
            if(expectedHeaderName == null){
                return true;
            }
            String requestHeaderValue = requestHeaders.get(expectedHeaderName);
            return expectedHeaderValue.equals(requestHeaderValue);
        }

        private void respond(HttpServletResponse response, RequestExpectation requestExpectation) throws FileNotFoundException, IOException {
            log.info("Responding to get for [" + requestMatchDescription() + "]");

            blockIfRequired();

            if (requestExpectation.responseRedirectDestination != null) {
                log.debug("Responding with redirect to '%s'", responseRedirectDestination);
                response.setStatus(302);
                response.addHeader("Location", responseRedirectDestination);
            }
            else if (requestExpectation.responseBody != null) {
                log.debug("Responding with '%d' and body '%s'", statusCode, responseBody);
                response.setStatus(statusCode);
                copyContentsToResponse(response, new ByteArrayInputStream(responseBody.getBytes("UTF-8")));
            }
            else {
                log.debug("Responding with '%d' and body from file '%s'", statusCode, responseFilename);
                response.setStatus(statusCode);
                copyContentsToResponse(response, new FileInputStream(responseFilename));
            }
            log.debug("Responded to request");
        }

		private String requestMatchDescription() {
			if (matchAnyRequest) {
				return "matching any request";
			}
			return expectedPath;
		}

        private void copyContentsToResponse(HttpServletResponse response, InputStream responseData) throws IOException {
            response.setContentType(responseMimeType);
            try {
                IOUtils.copy(responseData, response.getOutputStream());
                response.setStatus(statusCode);
            }
            finally {
                IOUtils.closeQuietly(responseData);
            }
        }

        private void blockIfRequired() {
            if (millisecondsToBlock > 0) {
                log.info("Blocking for %s ms before responding to this request", millisecondsToBlock);
                try {
                    Thread.sleep(millisecondsToBlock);
                } catch (InterruptedException e) {
                    log.warn(e, "StubHttpServer interrupted while blocking.");
                }
            }
            else {
                log.debug("Not blocking before responding to this request");
            }
        }

        public boolean isMatched() {
            return matched;
        }

        public void andRespondWithError(int statusCode) {
            this.statusCode = statusCode;
        }

		@Override
		public String toString() {
			return "RequestExpectation [expectedPath=" + requestMatchDescription()
					+ ", millisecondsToBlock=" + millisecondsToBlock
					+ ", responseMimeType=" + responseMimeType
					+ ", responseFilename=" + responseFilename
					+ ", responseBody=" + responseBody
					+ ", responseRedirectDestination="
					+ responseRedirectDestination + ", matchAnyRequest="
					+ matchAnyRequest + ", matched=" + matched
					+ ", expectedHeaderName=" + expectedHeaderName
					+ ", expectedHeaderValue=" + expectedHeaderValue
					+ ", requestBodyMatcher=" + requestBodyMatcher
					+ ", statusCode=" + statusCode + "]";
		}
        
    
    }

    private class BodyMatcher {
        public boolean matches(String requestBody) {
            return false;
        }
    }
}
