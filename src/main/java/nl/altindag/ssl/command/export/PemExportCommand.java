package nl.altindag.ssl.command.export;

import nl.altindag.ssl.command.SharedProperties;
import nl.altindag.ssl.util.CertificateUtils;
import nl.altindag.ssl.util.Utils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.*;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Command(name = "pem", description = "Export the extracted certificate to a base64 encoded string also known as PEM")
public class PemExportCommand implements Runnable {

    @Mixin
    private SharedProperties sharedProperties;

    @Option(names = {"-d", "--destination"}, description = "Destination of the to be stored file. Default is current directory if none is provided.")
    private String destination = System.getProperty("user.dir");

    @Override
    public void run() {
        Map<String, String> aliasToCertificate = sharedProperties.getUrlsToCertificates().values().stream()
                .flatMap(Collection::stream)
                .collect(collectingAndThen(collectingAndThen(toList(), Utils::generateAliases),
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

}
