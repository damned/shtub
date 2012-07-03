package shtub;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface RequestHandler {
    boolean handle(Url requestUriWithParams, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
