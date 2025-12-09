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

import nl.altindag.crip.command.VersionProvider;
import nl.altindag.crip.model.CertificateHolder;
import nl.altindag.crip.util.IOUtils;
import nl.altindag.crip.util.StatisticsUtils;
import nl.altindag.crip.util.UriUtils;
import nl.altindag.ssl.util.CertificateUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Command(name = "pem",
        description = "Export the extracted certificate to a base64 encoded string also known as PEM",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class)
public class PemExportCommand extends CombinableFileExport implements Runnable {

    @Option(names = {"--include-header"}, description = "Indicator to either omit or include additional information above the BEGIN statement.")
    protected Boolean includeHeader = true;

    private int counter = 0;

    public void run() {
        Map<String, String> filenameToCertificate;
        CertificateHolder certificateHolder = sharedProperties.getCertificateHolder();
        Map<String, List<X509Certificate>> urlsToCertificates = certificateHolder.getUrlsToCertificates();
        if (urlsToCertificates.isEmpty()) {
            return;
        }

        if (combined) {
            if (urlsToCertificates.size() == 1) {
                List<X509Certificate> certificates = certificateHolder.getUniqueCertificates();
                Path destination = null;

                if (!certificates.isEmpty()) {
                    String certificatesAsPem = certificates.stream()
                            .map(CertificateUtils::convertToPem)
                            .map(certificate -> includeHeader ? certificate : removeHeader(certificate))
                            .collect(Collectors.joining(System.lineSeparator()));

                    String key = urlsToCertificates.keySet().stream()
                            .findFirst()
                            .orElseThrow(IllegalArgumentException::new);

                    String fileName = Optional.ofNullable(UriUtils.extractHost(key))
                            .map(this::reformatFileName)
                            .orElse(key) + ".crt";

                    destination = getDestination()
                            .map(path -> resolveDestination(path, fileName))
                            .orElseGet(() -> getCurrentDirectory().resolve(fileName));

                    IOUtils.write(destination, certificatesAsPem.getBytes(StandardCharsets.UTF_8));
                }
                StatisticsUtils.printStatics(certificateHolder, destination);
                return;
            }

            filenameToCertificate = new HashMap<>();
            for (Entry<String, List<X509Certificate>> entry : urlsToCertificates.entrySet()) {
                String fileName = Optional.ofNullable(UriUtils.extractHost(entry.getKey()))
                        .map(this::reformatFileName)
                        .orElse(entry.getKey());

                if (filenameToCertificate.containsKey(fileName)) {
                    fileName = fileName + "-" + counter++;
                }

                List<X509Certificate> certificates = entry.getValue();
                if (!certificates.isEmpty()) {
                    String certificateAsPem = certificates.stream()
                            .map(CertificateUtils::convertToPem)
                            .map(certificate -> includeHeader ? certificate : removeHeader(certificate))
                            .collect(Collectors.joining(System.lineSeparator()));
                    filenameToCertificate.put(fileName, certificateAsPem);
                }
            }
        } else {
            filenameToCertificate = urlsToCertificates.values().stream()
                    .flatMap(Collection::stream)
                    .collect(collectingAndThen(collectingAndThen(toList(), CertificateUtils::generateAliases),
                            entry -> entry.entrySet().stream().collect(toMap(Entry::getKey, element -> includeHeader ? CertificateUtils.convertToPem(element.getValue()) : removeHeader(CertificateUtils.convertToPem(element.getValue())))))
                    );
        }

        Path directory = getDestination().map(this::resolveDestination).orElseGet(this::getCurrentDirectory);
        for (Entry<String, String> certificateEntry : filenameToCertificate.entrySet()) {
            Path certificatePath = directory.resolve(certificateEntry.getKey() + ".crt");
            IOUtils.write(certificatePath, certificateEntry.getValue().getBytes(StandardCharsets.UTF_8));
        }

        StatisticsUtils.printStatics(certificateHolder, directory);
    }

    private Path resolveDestination(Path path) {
        if (Files.isDirectory(path)) {
            return path;
        }

        if (!path.isAbsolute()) {
            return resolveDestination(path.toAbsolutePath().normalize());
        }

        return getCurrentDirectory();
    }

    private Path resolveDestination(Path path, String fileName) {
        if (Files.isDirectory(path)) {
            return path.resolve(fileName);
        }

        if (!path.isAbsolute()) {
            return resolveDestination(path.toAbsolutePath().normalize(), fileName);
        }

        return path;
    }

    private static String removeHeader(String value) {
        return Stream.of(value.split(System.lineSeparator()))
                .skip(2)
                .collect(Collectors.joining(System.lineSeparator()));
    }

}
