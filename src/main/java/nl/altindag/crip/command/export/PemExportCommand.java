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
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
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

    @Option(names = {"-p", "--pristine"}, description = "Indicator to either omit or include additional information above the BEGIN statement.")
    protected Boolean pristine = false;

    public void run() {
        Map<String, String> filenameToCertificate;

        if (combined) {
            if (sharedProperties.getUrls().size() == 1) {
                List<X509Certificate> certificates = sharedProperties.getCertificates();

                String certificatesAsPem = certificates.stream()
                        .map(nl.altindag.ssl.util.CertificateUtils::convertToPem)
                        .map(certificate -> pristine ? removeHeader(certificate) : certificate)
                        .collect(Collectors.joining(System.lineSeparator()));

                Path destination = getDestination()
                        .orElseGet(() -> IOUtils.getCurrentDirectory().resolve(CertificateUtils.extractHostFromUrl(sharedProperties.getUrls().get(0)) + ".crt"));

                IOUtils.write(destination, certificatesAsPem);
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

        if (pristine) {
            filenameToCertificate = filenameToCertificate.entrySet().stream()
                    .map(entry -> new SimpleEntry<>(entry.getKey(), removeHeader(entry.getValue())))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }

        for (Entry<String, String> certificateEntry : filenameToCertificate.entrySet()) {
            Path certificatePath = getDestination().orElseGet(IOUtils::getCurrentDirectory).resolve(certificateEntry.getKey());
            IOUtils.write(certificatePath, certificateEntry.getValue());
        }

        System.out.println("Successfully Exported certificates");
    }

    private static String removeHeader(String value) {
        return Stream.of(value.split(System.lineSeparator()))
                .skip(2)
                .collect(Collectors.joining(System.lineSeparator()));
    }

}
