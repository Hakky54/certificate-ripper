package nl.altindag.ssl.command;

import nl.altindag.ssl.util.KeyStoreUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Command(name = "export", description = "Export the extracted certificate to a PKCS12/p12 type truststore")
public class ExportCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportCommand.class);

    @Mixin
    private SharedProperties sharedProperties;

    @Option(names = {"-p", "--password"}, description = "TrustStore password. Default is changeit if none is provided.")
    private String password = "changeit";

    @Override
    public void run() {
        KeyStore trustStore = sharedProperties.getUrlsToCertificates().values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.collectingAndThen(Collectors.toList(), KeyStoreUtils::createTrustStore));

        Path trustStorePath = Paths.get(System.getProperty("user.dir"), "truststore.p12");

        try(OutputStream outputStream = Files.newOutputStream(trustStorePath, StandardOpenOption.CREATE)) {
            trustStore.store(outputStream, password.toCharArray());
            LOGGER.info("Exported certificates to {}", trustStorePath.toAbsolutePath());
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            LOGGER.warn("Failed to export the certificates", e);
        }
    }

}
