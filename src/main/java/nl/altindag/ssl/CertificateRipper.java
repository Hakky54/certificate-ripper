package nl.altindag.ssl;

import nl.altindag.ssl.util.CertificateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CertificateRipper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateRipper.class);
    private static final String CERTIFICATE_DELIMITER = "%n%n========== NEXT CERTIFICATE FOR %s ==========%n%n";

    public static void main(String[] applicationArguments) {
        boolean shouldBePemFormatted = Arrays.stream(applicationArguments)
                .map(String::toLowerCase)
                .anyMatch(argument -> argument.contains("format=pem"));

        if (shouldBePemFormatted) {
            extractCertificates(applicationArguments, CertificateUtils::getCertificateAsPem)
                    .forEach((String url, List<String> certificate) ->
                            LOGGER.info("Url = {}\n\n{}\n\n",
                                    url,
                                    String.join(String.format(CERTIFICATE_DELIMITER, url), certificate)));
        } else {
            extractCertificates(applicationArguments, CertificateUtils::getCertificate)
                    .forEach((String url, List<Certificate> certificates) ->
                            LOGGER.info("Url = {}\n\n{}\n\n",
                                    url,
                                    certificates.stream()
                                            .map(Certificate::toString)
                                            .collect(Collectors.joining(String.format(CERTIFICATE_DELIMITER, url)))
                            ));
        }
    }

    private static <T> Map<String, List<T>> extractCertificates(String[] applicationArguments,
                                                                Function<List<String>, Map<String, List<T>>> urlsToCertificates) {
        return Arrays.stream(applicationArguments)
                .map(String::toLowerCase)
                .filter(argument -> argument.contains("url="))
                .map(parameter -> parameter.split("url=")[1])
                .collect(Collectors.collectingAndThen(Collectors.toList(), urlsToCertificates));
    }

}
