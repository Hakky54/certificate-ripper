package nl.altindag.ssl.command;

import nl.altindag.ssl.util.CertificateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("FieldMayBeFinal")
@Command(name = "print", description = "Prints the extracted certificates to the console")
public class PrintCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrintCommand.class);
    private static final String CERTIFICATE_DELIMITER = "%n%n========== NEXT CERTIFICATE FOR %s ==========%n%n";

    @Mixin
    private SharedProperties sharedProperties;

    @Option(names = {"-f", "--format"}, description = "To be printed certificate format")
    private Format format = Format.X509;

    @Override
    public void run() {
        if (format == Format.X509) {
            sharedProperties.getUrlsToCertificates().forEach((String url, List<Certificate> certificates) ->
                    LOGGER.info("Certificates for url = {}\n\n{}\n\n",
                            url,
                            certificates.stream()
                                    .map(Certificate::toString)
                                    .collect(Collectors.joining(String.format(CERTIFICATE_DELIMITER, url)))
                    ));

        } else if (format == Format.PEM) {
            sharedProperties.getUrlsToCertificates().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            certificates -> CertificateUtils.convertToPem(certificates.getValue())))
                    .forEach((String path, List<String> certificate) ->
                            LOGGER.info("Certificates for url = {}\n\n{}\n\n",
                                    path,
                                    String.join(String.format(CERTIFICATE_DELIMITER, path), certificate)));
        }
    }

    private enum Format {
        PEM, X509
    }

}
