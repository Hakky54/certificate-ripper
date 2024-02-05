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
package nl.altindag.crip.util;

import nl.altindag.crip.model.CertificateHolder;
import nl.altindag.ssl.util.CertificateUtils;

import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class StatisticsUtils {

    private StatisticsUtils() {

    }

    public static void printStatics(CertificateHolder certificateHolder) {
        printStatics(certificateHolder, null);
    }

    public static void printStatics(CertificateHolder certificateHolder, Path destination) {
        System.out.printf("%nCertificate ripper statistics:%n- Certificate count%n%n");
        certificateHolder.getUrlsToCertificates().forEach((url, certificates) -> {
            System.out.printf("  * %d: %s%n", certificates.size(), url);
            Map<String, X509Certificate> aliasToCertificate = CertificateUtils.generateAliases(certificates);
            aliasToCertificate.forEach((alias, certificate) -> System.out.printf("         [%s]%n", alias));
        });

        if (!certificateHolder.getDuplicateCertificates().isEmpty()) {
            System.out.printf("%n- Duplicate certificate count%n%n");
            certificateHolder.getDuplicateCertificates().stream()
                    .map(CertificateUtils::generateAlias)
                    .collect(Collectors.groupingBy(Function.identity(), () -> new TreeMap<>(Comparator.comparing(String::toString)), Collectors.counting()))
                    .forEach((alias, count) -> System.out.printf("  * %d: [%s]%n", count, alias));
        }

        System.out.println();

        if (destination != null && !certificateHolder.getAllCertificates().isEmpty()) {
            String duplicateMessage = certificateHolder.getDuplicateCertificates().isEmpty() ? "" : String.format(", while also filtering out %d duplicates which resulted into %d unique certificates", certificateHolder.getDuplicateCertificates().size(), certificateHolder.getUniqueCertificates().size());
            System.out.printf("Extracted %d certificates%s.%nIt has been exported to %s%n", certificateHolder.getAllCertificates().size(), duplicateMessage, destination.toAbsolutePath());
        }
    }

}
