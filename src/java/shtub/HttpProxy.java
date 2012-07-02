package shtub;

public class HttpProxy {

    private StubHttpServer server;

    public HttpProxy(int proxyPort) {
        server = new StubHttpServer(proxyPort);
    }

    public HttpProxy stop() throws Exception {
        server.stop();
        return this;
    }

    public HttpProxy start() {
        server.respondUsing(new ProxyRequestHandler());
        server.start();
        return this;
    }

    public static void main(String[] args) {
        new HttpProxy(4545).start();
    }
}
