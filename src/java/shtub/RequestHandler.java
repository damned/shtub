package shtub;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface RequestHandler {
    boolean handle(String requestUriWithParams, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
