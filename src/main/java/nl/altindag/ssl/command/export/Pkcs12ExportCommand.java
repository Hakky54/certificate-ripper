package nl.altindag.ssl.command.export;

import nl.altindag.ssl.util.KeyStoreUtils;
import picocli.CommandLine.Command;
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

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Command(name = "pkcs12", description = "Export the extracted certificate to a PKCS12/p12 type truststore")
public class Pkcs12ExportCommand extends FileExport implements Runnable {

    @Option(names = {"-p", "--password"}, description = "TrustStore password. Default is changeit if none is provided.")
    private String password = "changeit";

    @Override
    public void run() {
        KeyStore trustStore = sharedProperties.getUrlsToCertificates().values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(collectingAndThen(toList(), KeyStoreUtils::createTrustStore));

        Path trustStorePath = Paths.get(destination, "truststore.p12");

        try(OutputStream outputStream = Files.newOutputStream(trustStorePath, StandardOpenOption.CREATE)) {
            trustStore.store(outputStream, password.toCharArray());
            System.out.println("Exported certificates to " + trustStorePath.toAbsolutePath());
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            System.err.println("Failed to export the certificates. Error message: " + e.getMessage());
        }
    }

}
