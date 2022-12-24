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

import nl.altindag.ssl.util.CertificateUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("FieldMayBeFinal")
@Command(name = "print", description = "Prints the extracted certificates to the console")
public class PrintCommand implements Runnable {

    private static final String CERTIFICATE_DELIMITER = "%n%n========== NEXT CERTIFICATE FOR %s ==========%n%n";

    @Mixin
    private SharedProperties sharedProperties;

    @Option(names = {"-f", "--format"}, description = "To be printed certificate format")
    private Format format = Format.X509;

    @Override
    public void run() {
        if (format == Format.X509) {
            sharedProperties.getUrlsToCertificates().forEach((String url, List<X509Certificate> certificates) ->
                    System.out.printf("Certificates for url = %s%n%n%s%n%n%n",
                            url,
                            certificates.stream()
                                    .map(X509Certificate::toString)
                                    .collect(Collectors.joining(String.format(CERTIFICATE_DELIMITER, url)))
                    ));

        } else if (format == Format.PEM) {
            sharedProperties.getUrlsToCertificates().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            certificates -> CertificateUtils.convertToPem(certificates.getValue())))
                    .forEach((String path, List<String> certificate) ->
                            System.out.printf("Certificates for url = %s%n%n%s%n%n",
                                    path,
                                    String.join(String.format(CERTIFICATE_DELIMITER, path), certificate)));
        }
    }

    private enum Format {
        PEM, X509
    }

}
