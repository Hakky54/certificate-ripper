package nl.altindag.crip.util;

import java.net.MalformedURLException;
import java.net.URL;

public final class HostnameUtils {

    private HostnameUtils() {

    }

    public static String extractHostFromUrl(String value) {
        try {
            URL url = new URL(value);
            return url.getHost();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
