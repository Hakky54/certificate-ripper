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
import picocli.CommandLine.Option;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Command(name = "pem", description = "Export the extracted certificate to a base64 encoded string also known as PEM")
public class PemExportCommand extends CombinableFileExport implements Runnable {

    @Option(names = {"--include-header"}, description = "Indicator to either omit or include additional information above the BEGIN statement.")
    protected Boolean includeHeader = true;

    private int counter = 0;

    public void run() {
        Map<String, String> filenameToCertificate;
        CertificateHolder certificateHolder = sharedProperties.getCertificateHolder();

        if (combined) {
            if (sharedProperties.getUrls().size() == 1) {
                List<X509Certificate> certificates = sharedProperties.getCertificatesFromFirstUrl();
                Path destination = null;

                if (!certificates.isEmpty()) {
                    String certificatesAsPem = certificates.stream()
                            .map(CertificateUtils::convertToPem)
                            .map(certificate -> includeHeader ? certificate : removeHeader(certificate))
                            .collect(Collectors.joining(System.lineSeparator()));

                    destination = getDestination()
                            .orElseGet(() -> getCurrentDirectory()
                                    .resolve(reformatFileName(UriUtils.extractHost(sharedProperties.getUrls().get(0))) + ".crt"));

                    IOUtils.write(destination, certificatesAsPem.getBytes(StandardCharsets.UTF_8));
                }
                StatisticsUtils.printStatics(certificateHolder, destination);
                return;
            }

            filenameToCertificate = new HashMap<>();
            for (Entry<String, List<X509Certificate>> entry : certificateHolder.getUrlsToCertificates().entrySet()) {
                String fileName = reformatFileName(UriUtils.extractHost(entry.getKey()));
                if (filenameToCertificate.containsKey(fileName)) {
                    fileName = fileName + "-" + counter++;
                }

                List<X509Certificate> certificates = entry.getValue();
                if (!certificates.isEmpty()) {
                    String certificateAsPem = String.join(System.lineSeparator(), CertificateUtils.convertToPem(certificates));
                    filenameToCertificate.put(fileName, certificateAsPem);
                }
            }
        } else {
            filenameToCertificate = certificateHolder.getUrlsToCertificates().values().stream()
                    .flatMap(Collection::stream)
                    .collect(collectingAndThen(collectingAndThen(toList(), CertificateUtils::generateAliases),
                            entry -> entry.entrySet().stream().collect(toMap(Entry::getKey, element -> CertificateUtils.convertToPem(element.getValue())))));
        }

        if (!includeHeader) {
            filenameToCertificate = filenameToCertificate.entrySet().stream()
                    .map(entry -> new SimpleEntry<>(entry.getKey(), removeHeader(entry.getValue())))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }

        Path directory = getDestination().orElseGet(this::getCurrentDirectory);
        for (Entry<String, String> certificateEntry : filenameToCertificate.entrySet()) {
            Path certificatePath = directory.resolve(certificateEntry.getKey() + ".crt");
            IOUtils.write(certificatePath, certificateEntry.getValue().getBytes(StandardCharsets.UTF_8));
        }

        StatisticsUtils.printStatics(certificateHolder, directory);
    }

    private static String removeHeader(String value) {
        return Stream.of(value.split(System.lineSeparator()))
                .skip(2)
                .collect(Collectors.joining(System.lineSeparator()));
    }

}
