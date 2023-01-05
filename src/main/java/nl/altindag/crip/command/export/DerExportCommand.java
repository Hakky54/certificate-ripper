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

import nl.altindag.crip.util.CertificateUtils;
import nl.altindag.crip.util.IOUtils;
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

@Command(name = "der", description = "Export the extracted certificate to a binary form also known as DER")
public class DerExportCommand extends CombinableFileExport implements Runnable {

    private int counter = 0;

    @Override
    public void run() {
        try {
            Map<String, byte[]> filenameToFileContent = new HashMap<>();

            if (combined) {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

                if (sharedProperties.getUrls().size() == 1) {
                    List<X509Certificate> certificates = sharedProperties.getCertificates();
                    CertPath certPath = certificateFactory.generateCertPath(certificates);

                    Path destination = getDestination()
                            .orElseGet(() -> IOUtils.getCurrentDirectory().resolve(CertificateUtils.extractHostFromUrl(sharedProperties.getUrls().get(0)) + ".p7b"));

                    IOUtils.write(destination, certPath.getEncoded("PKCS7"));
                    System.out.println("Successfully Exported certificates");
                    return;
                }

                for (Entry<String, List<X509Certificate>> entry : sharedProperties.getUrlsToCertificates().entrySet()) {
                    String fileName = CertificateUtils.extractHostFromUrl(entry.getKey());
                    if (filenameToFileContent.containsKey(fileName)) {
                        fileName = fileName + "-" + counter++;
                    }
                    CertPath certPath = certificateFactory.generateCertPath(entry.getValue());

                    filenameToFileContent.put(fileName, certPath.getEncoded("PKCS7"));
                }

                filenameToFileContent = filenameToFileContent.entrySet().stream()
                        .collect(Collectors.toMap(entry -> entry.getKey() + ".p7b", Map.Entry::getValue));
            } else {
                Map<String, X509Certificate> aliasToCertificate = sharedProperties.getUrlsToCertificates().values().stream()
                        .flatMap(Collection::stream)
                        .collect(collectingAndThen(toList(), CertificateUtils::generateAliases));

                for (Entry<String, X509Certificate> entry : aliasToCertificate.entrySet()) {
                    filenameToFileContent.put(entry.getKey() + ".crt", entry.getValue().getEncoded());
                }
            }

            for (Entry<String, byte[]> certificateEntry : filenameToFileContent.entrySet()) {
                Path destination = getDestination().orElseGet(IOUtils::getCurrentDirectory).resolve(certificateEntry.getKey());
                IOUtils.write(destination, certificateEntry.getValue());
            }

        } catch (CertificateException e) {
            System.err.println("Failed to export the certificates. Error message: " + e.getMessage());
        }

        System.out.println("Successfully Exported certificates");
    }

}
