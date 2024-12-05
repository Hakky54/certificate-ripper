/*
 * Copyright 2021 Thunderberry.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.altindag.crip.command.export;

import nl.altindag.crip.model.CertificateHolder;
import nl.altindag.crip.util.StatisticsUtils;
import nl.altindag.ssl.util.CertificateUtils;
import nl.altindag.ssl.util.internal.IOUtils;
import nl.altindag.ssl.util.internal.UriUtils;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@Command(name = "der",
        description = "Export the extracted certificate to a binary form also known as DER",
        mixinStandardHelpOptions = true)
public class DerExportCommand extends CombinableFileExport implements Runnable {

    private int counter = 0;

    @Override
    public void run() {
        CertificateHolder certificateHolder = sharedProperties.getCertificateHolder();
        try {
            Map<String, byte[]> filenameToFileContent = new HashMap<>();

            if (combined) {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

                if (sharedProperties.getUrls().size() == 1) {
                    List<X509Certificate> certificates = sharedProperties.getCertificatesFromFirstUrl();
                    Path destination = null;

                    if (!certificates.isEmpty()) {
                        CertPath certPath = certificateFactory.generateCertPath(certificates);

                        String fileName = UriUtils.extractHost(sharedProperties.getUrls().get(0)) + ".p7b";
                        destination = getDestination().orElseGet(() -> getCurrentDirectory().resolve(fileName));

                        IOUtils.write(destination, certPath.getEncoded("PKCS7"));
                    }

                    StatisticsUtils.printStatics(certificateHolder, destination);
                    return;
                }

                for (Entry<String, List<X509Certificate>> entry : sharedProperties.getCertificateHolder().getUrlsToCertificates().entrySet()) {
                    String fileName = reformatFileName(UriUtils.extractHost(entry.getKey()));
                    if (filenameToFileContent.containsKey(fileName)) {
                        fileName = fileName + "-" + counter++;
                    }

                    List<X509Certificate> certificates = entry.getValue();
                    if (!certificates.isEmpty()) {
                        CertPath certPath = certificateFactory.generateCertPath(certificates);
                        filenameToFileContent.put(fileName, certPath.getEncoded("PKCS7"));
                    }
                }

                filenameToFileContent = filenameToFileContent.entrySet().stream()
                        .collect(Collectors.toMap(entry -> entry.getKey() + ".p7b", Map.Entry::getValue));
            } else {
                Map<String, X509Certificate> aliasToCertificate = sharedProperties.getCertificateHolder().getUrlsToCertificates().values().stream()
                        .flatMap(Collection::stream)
                        .collect(collectingAndThen(toList(), CertificateUtils::generateAliases));

                for (Entry<String, X509Certificate> entry : aliasToCertificate.entrySet()) {
                    filenameToFileContent.put(entry.getKey() + ".crt", entry.getValue().getEncoded());
                }
            }

            Path directory = getDestination().orElseGet(this::getCurrentDirectory);
            for (Entry<String, byte[]> certificateEntry : filenameToFileContent.entrySet()) {
                Path certificatePath = directory.resolve(certificateEntry.getKey());
                IOUtils.write(certificatePath, certificateEntry.getValue());
            }

            StatisticsUtils.printStatics(certificateHolder, directory);
        } catch (CertificateException e) {
            System.err.println("Failed to export the certificates. Error message: " + e.getMessage());
        }
    }

}
