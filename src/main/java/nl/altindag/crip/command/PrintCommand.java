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
package nl.altindag.crip.command;

import nl.altindag.crip.util.StatisticsUtils;
import nl.altindag.ssl.util.CertificateUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.security.cert.X509Certificate;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("FieldMayBeFinal")
@Command(name = "print", description = "Prints the extracted certificates to the console")
public class PrintCommand implements Runnable {

    private static final String CERTIFICATE_DELIMITER = "%n%n<========== Next certificate for %s ==========>%n%n";

    @Mixin
    private SharedProperties sharedProperties;

    @Option(names = {"-f", "--format"}, description = "To be printed certificate format")
    private Format format = Format.X509;

    @Override
    public void run() {
        Map<String, List<X509Certificate>> urlsToCertificates = sharedProperties.getCertificateHolder().getUrlsToCertificates();
        StatisticsUtils.printStatics(sharedProperties.getCertificateHolder());

        switch (format) {
            case X509:
                urlsToCertificates.entrySet().stream()
                        .filter(entry -> !entry.getValue().isEmpty())
                        .forEach(entry ->
                                System.out.printf("Certificates for url = %s%n%n%s%n%n%n",
                                        entry.getKey(),
                                        entry.getValue().stream()
                                                .map(X509Certificate::toString)
                                                .collect(Collectors.joining(String.format(CERTIFICATE_DELIMITER, entry.getKey())))
                                ));
                break;
            case PEM:
                urlsToCertificates.entrySet().stream()
                        .filter(entry -> !entry.getValue().isEmpty())
                        .map(entry -> new SimpleImmutableEntry<>(entry.getKey(), CertificateUtils.convertToPem(entry.getValue())))
                        .forEach(entry ->
                                System.out.printf("Certificates for url = %s%n%n%s%n%n",
                                        entry.getKey(),
                                        String.join(String.format(CERTIFICATE_DELIMITER, entry.getKey()), entry.getValue())));
                break;
        }
    }

    private enum Format {
        PEM, X509
    }

}
