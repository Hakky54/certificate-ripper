package nl.altindag.crip.command.export;

import nl.altindag.crip.util.HostnameUtils;
import nl.altindag.crip.util.AliasUtils;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@Command(name = "der", description = "Export the extracted certificate to a binary form also known as DER")
public class DerExportCommand extends CombinableFileExport implements Runnable {

    @Override
    public void run() {
        try {
            Map<String, List<X509Certificate>> urlsToCertificates = sharedProperties.getUrlsToCertificates();
            Map<String, byte[]> filenameToFileContent = new HashMap<>();

            if (combined) {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

                for (Entry<String, List<X509Certificate>> entry : urlsToCertificates.entrySet()) {
                    String host = HostnameUtils.extractHostFromUrl(entry.getKey());
                    CertPath certPath = certificateFactory.generateCertPath(entry.getValue());

                    filenameToFileContent.put(host + ".p7b", certPath.getEncoded("PKCS7"));
                }
            } else {
                Map<String, X509Certificate> aliasToCertificate = urlsToCertificates.values().stream()
                        .flatMap(Collection::stream)
                        .collect(collectingAndThen(toList(), AliasUtils::generateAliases));

                for (Entry<String, X509Certificate> entry : aliasToCertificate.entrySet()) {
                    filenameToFileContent.put(entry.getKey() + ".crt", entry.getValue().getEncoded());
                }
            }

            for (Entry<String, byte[]> certificateEntry : filenameToFileContent.entrySet()) {
                Path certificatePath = Paths.get(destination, certificateEntry.getKey());
                Files.write(certificatePath, certificateEntry.getValue(), StandardOpenOption.CREATE);
            }

        } catch (CertificateException | IOException e) {
            System.err.println("Failed to export the certificates. Error message: " + e.getMessage());
        }

        System.out.println("Exported certificates to " + Paths.get(destination).toAbsolutePath());
    }

}
