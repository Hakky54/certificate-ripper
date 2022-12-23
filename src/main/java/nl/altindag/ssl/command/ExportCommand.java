package nl.altindag.ssl.command;

import nl.altindag.ssl.util.CertificateUtils;
import nl.altindag.ssl.util.KeyStoreUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Command(name = "export", description = "Export the extracted certificate to a PKCS12/p12 type truststore")
public class ExportCommand implements Runnable {

    @Mixin
    private SharedProperties sharedProperties;

    @Option(names = {"-p", "--password"}, description = "TrustStore password. Default is changeit if none is provided.")
    private String password = "changeit";

    @Option(names = {"-d", "--destination"}, description = "Destination of the to be stored truststore file. Default is current directory if none is provided.")
    private String destination = System.getProperty("user.dir");

    @Option(names = {"-f", "--format"}, description = "")
    private Format format = Format.PKCS12;

    @Override
    public void run() {
        switch (format) {
            case PKCS12:
                writeToPkcs12();
                break;
            case DER:
                writeToDer();
                break;
            case PEM:
                writeToPem();
                break;
        }
    }

    private void writeToPkcs12() {
        KeyStore trustStore = sharedProperties.getUrlsToCertificates().values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(collectingAndThen(Collectors.toList(), KeyStoreUtils::createTrustStore));

        Path trustStorePath = Paths.get(destination, "truststore.p12");

        try(OutputStream outputStream = Files.newOutputStream(trustStorePath, StandardOpenOption.CREATE)) {
            trustStore.store(outputStream, password.toCharArray());
            System.out.println("Exported certificates to " + trustStorePath.toAbsolutePath());
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            System.err.println("Failed to export the certificates. Error message: " + e.getMessage());
        }
    }

    private void writeToDer() {
        Map<String, X509Certificate> aliasToCertificate = sharedProperties.getUrlsToCertificates().values().stream()
                .flatMap(Collection::stream)
                .collect(collectingAndThen(Collectors.toList(), this::generateAliases));

        for (Entry<String, X509Certificate> certificateEntry : aliasToCertificate.entrySet()) {
            Path certificatePath = Paths.get(destination, certificateEntry.getKey() + ".crt");
            try {
                byte[] encoded = certificateEntry.getValue().getEncoded();
                Files.write(certificatePath, encoded, StandardOpenOption.CREATE);
            } catch (IOException | CertificateException e) {
                System.err.println("Failed to export the certificates. Error message: " + e.getMessage());
            }
        }

        System.out.println("Exported certificates to " + Paths.get(destination).toAbsolutePath());
    }

    private void writeToPem() {
        Map<String, String> aliasToCertificate = sharedProperties.getUrlsToCertificates().values().stream()
                .flatMap(Collection::stream)
                .collect(collectingAndThen(collectingAndThen(
                        Collectors.toList(), this::generateAliases),
                        entry -> entry.entrySet().stream().collect(toMap(Entry::getKey, element -> CertificateUtils.convertToPem(element.getValue())))));

        for (Entry<String, String> certificateEntry : aliasToCertificate.entrySet()) {
            Path certificatePath = Paths.get(destination, certificateEntry.getKey() + ".pem");
            try {
                Files.write(certificatePath, certificateEntry.getValue().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            } catch (IOException e) {
                System.err.println("Failed to export the certificates. Error message: " + e.getMessage());
            }
        }

        System.out.println("Exported certificates to " + Paths.get(destination).toAbsolutePath());
    }

    private Map<String, X509Certificate> generateAliases(List<X509Certificate> certificates) {
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

    enum Format {
        PKCS12, DER, PEM
    }

}
