package shtub;

import java.util.List;

public class UnhandledRequestException extends RuntimeException {
    public UnhandledRequestException(String requestUri, List<StubHttpServer.RequestExpectation> expectations) {
    }
}
