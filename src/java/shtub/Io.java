package shtub;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;

public class Io {
    static String stringOf(InputStream requestInputStream) throws IOException {
        ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream();
        IOUtils.copy(requestInputStream, bodyBytes);
        return new String(bodyBytes.toByteArray(), "UTF-8");
    }
}
