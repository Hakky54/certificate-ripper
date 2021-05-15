package nl.altindag.ssl;

import nl.altindag.ssl.util.CertificateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CertificateRipper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateRipper.class);

    private static final String NEW_LINE = System.lineSeparator();
    private static final String CERTIFICATE_DELIMITER = NEW_LINE + NEW_LINE + "========== NEXT CERTIFICATE FOR %s ==========" + NEW_LINE + NEW_LINE;

    public static void main(String[] applicationArguments) {
        boolean shouldBePemFormatted = Arrays.stream(applicationArguments)
                .map(String::toLowerCase)
                .anyMatch(argument -> argument.contains("format=pem"));

        if (shouldBePemFormatted) {
            extractAndConsumeCertificates(applicationArguments, CertificateUtils::getCertificateAsPem, (key, value) ->
                    LOGGER.info("Url = {}\n\n{}\n\n",
                            key,
                            String.join(String.format(CERTIFICATE_DELIMITER, key), value)));
        } else {
            extractAndConsumeCertificates(applicationArguments, CertificateUtils::getCertificate, (key, value) ->
                    LOGGER.info("Url = {}\n\n{}\n\n",
                            key,
                            value.stream()
                                    .map(Certificate::toString)
                                    .collect(Collectors.joining(String.format(CERTIFICATE_DELIMITER, key)))
                            )
            );
        }
    }

    private static <T> void extractAndConsumeCertificates(String[] applicationArguments,
                                                          Function<List<String>, Map<String, List<T>>> urlsToCertificates,
                                                          BiConsumer<String, List<T>> certificateConsumer) {
        Arrays.stream(applicationArguments)
                .map(String::toLowerCase)
                .filter(argument -> argument.contains("url="))
                .map(parameter -> parameter.split("url=")[1])
                .collect(Collectors.collectingAndThen(Collectors.toList(), urlsToCertificates))
                .forEach(certificateConsumer);
    }

}
