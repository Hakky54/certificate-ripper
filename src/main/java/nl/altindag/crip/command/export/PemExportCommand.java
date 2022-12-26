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
import picocli.CommandLine.Command;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Command(name = "pem", description = "Export the extracted certificate to a base64 encoded string also known as PEM")
public class PemExportCommand extends CombinableFileExport implements Runnable {

    public void run() {
        Map<String, String> filenameToCertificate;

        if (combined) {
            if (sharedProperties.getUrls().size() == 1) {
                List<X509Certificate> certificates = sharedProperties.getCertificates();

                String certificatesAsPem = certificates.stream()
                        .map(nl.altindag.ssl.util.CertificateUtils::convertToPem)
                        .collect(Collectors.joining(System.lineSeparator()));

                Path destination = getDestination()
                        .orElseGet(() -> getCurrentDirectory().resolve(CertificateUtils.extractHostFromUrl(sharedProperties.getUrls().get(0)) + ".crt"));

                write(destination, certificatesAsPem.getBytes(StandardCharsets.UTF_8));
                System.out.println("Successfully Exported certificates");
                return;
            }

            filenameToCertificate = sharedProperties.getUrlsToCertificates().entrySet().stream()
                    .map(entry -> new SimpleEntry<>(CertificateUtils.extractHostFromUrl(entry.getKey()) + ".crt", nl.altindag.ssl.util.CertificateUtils.convertToPem(entry.getValue())))
                    .collect(Collectors.toMap(SimpleEntry::getKey, entry -> String.join(System.lineSeparator(), entry.getValue())));
        } else {
            filenameToCertificate = sharedProperties.getUrlsToCertificates().values().stream()
                    .flatMap(Collection::stream)
                    .collect(collectingAndThen(collectingAndThen(toList(), CertificateUtils::generateAliases),
                            entry -> entry.entrySet().stream().collect(toMap(element -> element.getKey() + ".crt", element -> nl.altindag.ssl.util.CertificateUtils.convertToPem(element.getValue())))));
        }

        for (Entry<String, String> certificateEntry : filenameToCertificate.entrySet()) {
            Path certificatePath = getDestination().orElseGet(this::getCurrentDirectory).resolve(certificateEntry.getKey());
            write(certificatePath, certificateEntry.getValue().getBytes(StandardCharsets.UTF_8));
        }

        System.out.println("Successfully Exported certificates");
    }

}
