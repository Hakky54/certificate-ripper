package nl.altindag.ssl.command.export;

import nl.altindag.ssl.command.SharedProperties;
import nl.altindag.ssl.util.AliasUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Command(name = "der", description = "Export the extracted certificate to a binary form also known as DER")
public class DerExportCommand implements Runnable {

    @Mixin
    private SharedProperties sharedProperties;

    @Option(names = {"-d", "--destination"}, description = "Destination of the to be stored file. Default is current directory if none is provided.")
    private String destination = System.getProperty("user.dir");

    @Override
    public void run() {
        Map<String, X509Certificate> aliasToCertificate = sharedProperties.getUrlsToCertificates().values().stream()
                .flatMap(Collection::stream)
                .collect(collectingAndThen(toList(), AliasUtils::generateAliases));

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

}
