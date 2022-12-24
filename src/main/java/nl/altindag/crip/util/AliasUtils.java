package nl.altindag.crip.util;

import nl.altindag.ssl.util.CertificateUtils;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AliasUtils {

    private AliasUtils() {

    }

    public static Map<String, X509Certificate> generateAliases(List<X509Certificate> certificates) {
        Map<String, X509Certificate> aliasToCertificate = new HashMap<>();
        for (X509Certificate certificate : certificates) {
            String alias = CertificateUtils.generateAlias(certificate)
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

}
