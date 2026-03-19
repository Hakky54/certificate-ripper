/*
 * Copyright 2021 Thunderberry.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.altindag.crip.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <strong>NOTE:</strong>
 * Please don't use this class directly as it is part of the internal API. Class name and methods can be changed any time.
 *
 * @author Hakan Altindag
 */
public final class UriUtils {

    private static final Integer DNS_NAME_ID = 2;
    private static final String ASTERISKS_AND_DOT = "\\*\\.";

    private UriUtils() {}

    public static String extractHost(String value) {
        try {
            URI url = new URI(value);
            return url.getHost();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static int extractPort(String value) {
        try {
            URI url = new URI(value);
            return url.getPort();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Extracts the DNS Names from the Subject Alternative Name extension of the provided certificates.
     * And appends "https://" prefix to each DNS name.
     */
    public static List<String> extractHostsFromSAN(List<X509Certificate> certificates) {
        List<String> dnsNames = new ArrayList<>();
        for (X509Certificate certificate : certificates) {
            try {
                if (certificate.getSubjectAlternativeNames() == null) {
                    continue;
                }

                certificate.getSubjectAlternativeNames().stream()
                        .filter(sanEntry -> sanEntry.size() == 2)
                        .filter(sanEntry -> DNS_NAME_ID.equals(sanEntry.get(0)))
                        .map(sanEntry -> sanEntry.get(1))
                        .map(dnsName -> ((String) dnsName).replaceFirst(ASTERISKS_AND_DOT, ""))
                        .map(dnsName -> "https://" + dnsName)
                        .forEach(dnsNames::add);

            } catch (CertificateParsingException ignored) {}
        }
        return Collections.unmodifiableList(dnsNames);
    }

}
