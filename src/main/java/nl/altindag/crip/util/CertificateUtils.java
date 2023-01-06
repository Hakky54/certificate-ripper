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

import nl.altindag.crip.exception.CertificateRipperException;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CertificateUtils {

    private CertificateUtils() {

    }

    public static Map<String, X509Certificate> generateAliases(List<X509Certificate> certificates) {
        Map<String, X509Certificate> aliasToCertificate = new HashMap<>();
        for (X509Certificate certificate : certificates) {
            String alias = nl.altindag.ssl.util.CertificateUtils.generateAlias(certificate)
                    .toLowerCase(Locale.US)
                    .replaceAll(" ", "-")
                    .replaceAll(",", "_")
                    .replaceAll("\\*", "")
                    .replaceAll("\\.", "");

            boolean shouldAddCertificate = true;

            if (aliasToCertificate.containsKey(alias)) {
                for (int number = 0; number <= 1000; number++) {
                    String mayBeUniqueAlias = alias + "-" + number;
                    if (!aliasToCertificate.containsKey(mayBeUniqueAlias)) {
                        alias = mayBeUniqueAlias;
                        shouldAddCertificate = true;
                        break;
                    } else {
                        shouldAddCertificate = false;
                    }
                }
            }

            if (shouldAddCertificate) {
                aliasToCertificate.put(alias, certificate);
            }
        }
        return aliasToCertificate;
    }

    public static String extractHostFromUrl(String value) {
        try {
            URL url = new URL(value);
            return url.getHost();
        } catch (MalformedURLException e) {
            throw new CertificateRipperException(e);
        }
    }

}
