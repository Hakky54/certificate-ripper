package nl.altindag.ssl.command.export;

import nl.altindag.ssl.util.AliasUtils;
import nl.altindag.ssl.util.CertificateUtils;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.cert.X509Certificate;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Command(name = "pem", description = "Export the extracted certificate to a base64 encoded string also known as PEM")
public class PemExportCommand extends CombinableFileExport implements Runnable {

    public void run() {
        Map<String, List<X509Certificate>> urlsToCertificates = sharedProperties.getUrlsToCertificates();
        Map<String, String> filenameToCertificate;

        if (combined) {
            filenameToCertificate = urlsToCertificates.entrySet().stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(extractHostFromUrl(entry.getKey()) + ".pem", CertificateUtils.convertToPem(entry.getValue())))
                    .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, entry -> String.join(System.lineSeparator(), entry.getValue())));
        } else {
            filenameToCertificate = urlsToCertificates.values().stream()
                    .flatMap(Collection::stream)
                    .collect(collectingAndThen(collectingAndThen(toList(), AliasUtils::generateAliases),
                            entry -> entry.entrySet().stream().collect(toMap(element -> element.getKey() + ".pem", element -> CertificateUtils.convertToPem(element.getValue())))));
        }

        for (Entry<String, String> certificateEntry : filenameToCertificate.entrySet()) {
            Path certificatePath = Paths.get(destination, certificateEntry.getKey());
            try {
                Files.write(certificatePath, certificateEntry.getValue().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            } catch (IOException e) {
                System.err.println("Failed to export the certificates. Error message: " + e.getMessage());
            }
        }

        System.out.println("Exported certificates to " + Paths.get(destination).toAbsolutePath());
    }

}
