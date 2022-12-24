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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
        Map<String, List<X509Certificate>> urlsToCertificates = sharedProperties.getUrlsToCertificates();
        Map<String, String> filenameToCertificate;

        if (combined) {
            filenameToCertificate = urlsToCertificates.entrySet().stream()
                    .map(entry -> new SimpleEntry<>(CertificateUtils.extractHostFromUrl(entry.getKey()) + ".pem", nl.altindag.ssl.util.CertificateUtils.convertToPem(entry.getValue())))
                    .collect(Collectors.toMap(SimpleEntry::getKey, entry -> String.join(System.lineSeparator(), entry.getValue())));
        } else {
            filenameToCertificate = urlsToCertificates.values().stream()
                    .flatMap(Collection::stream)
                    .collect(collectingAndThen(collectingAndThen(toList(), CertificateUtils::generateAliases),
                            entry -> entry.entrySet().stream().collect(toMap(element -> element.getKey() + ".pem", element -> nl.altindag.ssl.util.CertificateUtils.convertToPem(element.getValue())))));
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
