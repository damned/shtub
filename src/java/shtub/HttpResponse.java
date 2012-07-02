package shtub;

import org.apache.http.Header;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HttpResponse {

    private org.apache.http.HttpResponse response;

    public HttpResponse(org.apache.http.HttpResponse response) {
        this.response = response;
    }

    public String body() {
        try {
            return Io.stringOf(response.getEntity().getContent());
        } catch (IOException e) {
            throw new ShtubException(e, "problem reading body");
        }
    }

    public Integer status() {
        return response.getStatusLine().getStatusCode();
    }

    public String contentType() {
        return response.getEntity().getContentType().getValue();
    }

    public boolean copyTo(HttpServletResponse outputResponse) throws Exception {

//        outputResponse.setStatus(status());
//        copyHeadersTo(outputResponse);
//        IOUtils.copy(new StringReader(body()), outputResponse.getWriter());

        outputResponse.setStatus(status());
        copyHeadersTo(outputResponse);
        writeBodyTo(outputResponse.getOutputStream());
        return true;
    }

    private void writeBodyTo(ServletOutputStream output) throws Exception {
        output.write(bodyBytes());
        output.flush();
    }

    public byte[] bodyBytes() throws Exception {
        return EntityUtils.toByteArray(response.getEntity());
    }

    private void copyHeadersTo(HttpServletResponse outputResponse) {
        for (Header header : response.getAllHeaders()) {
            outputResponse.addHeader(header.getName(), header.getValue());
        }
    }
}
