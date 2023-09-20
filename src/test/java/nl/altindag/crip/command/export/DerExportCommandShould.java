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

import nl.altindag.crip.command.FileBaseTest;
import nl.altindag.ssl.util.CertificateUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.altindag.crip.IOTestUtils.getResource;
import static org.assertj.core.api.Assertions.assertThat;

class DerExportCommandShould extends FileBaseTest {

    @Test
    void exportMultipleCertificateFromChainAsIndividualFiles() throws IOException {
        List<Certificate> expectedCertificates = Stream.concat(
                        CertificateUtils.loadCertificate(getResource("reference-files/der/server-one/cn=certificate-ripper-server-one_ou=amsterdam_o=thunderberry_c=nl.crt")).stream(),
                        CertificateUtils.loadCertificate(getResource("reference-files/der/server-one/cn=root-ca_ou=certificate-authority_o=thunderberry_c=nl.crt")).stream())
                .collect(Collectors.toList());

        assertThat(expectedCertificates).isNotEmpty();

        cmd.execute("export", "der", "--url=https://localhost:8443", "--destination=" + TEMP_DIRECTORY.toAbsolutePath());

        assertThat(consoleCaptor.getStandardOutput()).contains("Extracted 2 certificates.");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files)
                .hasSize(expectedCertificates.size())
                .allMatch(path -> path.toString().endsWith(".crt"));

        for (Path file : files) {
            byte[] content = Files.readAllBytes(file);
            List<Certificate> certificates = CertificateUtils.loadCertificate(new ByteArrayInputStream(content));
            assertThat(expectedCertificates).containsAll(certificates);
        }
    }

    @Test
    void exportMultipleCertificateFromChainAsSingleCombinedFile() throws IOException {
        List<Certificate> expectedCertificates = CertificateUtils.loadCertificate(getResource("reference-files/der/server-one/cn=certificate-ripper-server-one_ou=amsterdam_o=thunderberry_c=nl_combined.p7b"));
        assertThat(expectedCertificates).isNotEmpty().hasSize(2);

        cmd.execute("export", "der", "--url=https://localhost:8443", "--combined=true", "--destination=" + TEMP_DIRECTORY.toAbsolutePath().resolve("localhost.p7b"));

        assertThat(consoleCaptor.getStandardOutput()).contains("Extracted 2 certificates.");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files)
                .hasSize(1)
                .allMatch(path -> path.toString().endsWith(".p7b"));

        byte[] content = Files.readAllBytes(files.get(0));
        List<Certificate> certificates = CertificateUtils.loadCertificate(new ByteArrayInputStream(content));
        assertThat(certificates)
                .hasSize(2)
                .containsAll(expectedCertificates);
    }

    @Test
    void exportMultipleCertificateFromChainAsSingleCombinedFileWithMultipleUrls() throws IOException {
        List<Certificate> expectedCertificates = Stream.concat(
                CertificateUtils.loadCertificate(getResource("reference-files/der/server-one/cn=certificate-ripper-server-one_ou=amsterdam_o=thunderberry_c=nl_combined.p7b")).stream(),
                CertificateUtils.loadCertificate(getResource("reference-files/der/server-two/cn=certificate-ripper-server-two_ou=amsterdam_o=thunderberry_c=nl_combined.p7b")).stream()
        ).collect(Collectors.toList());

        cmd.execute("export", "der", "--url=https://localhost:8443", "--url=https://localhost:8444", "--combined=true", "--destination=" + TEMP_DIRECTORY.toAbsolutePath());

        assertThat(consoleCaptor.getStandardOutput()).contains("Extracted 4 certificates.");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files)
                .hasSize(2)
                .allMatch(path -> path.toString().endsWith(".p7b"));

        for (Path file : files) {
            byte[] content = Files.readAllBytes(file);
            List<Certificate> certificates = CertificateUtils.loadCertificate(new ByteArrayInputStream(content));
            assertThat(expectedCertificates).containsAll(certificates);
        }
    }

}
