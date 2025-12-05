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
package nl.altindag.crip.request.export;

import nl.altindag.crip.CertificateRipper;
import nl.altindag.crip.command.FileBaseTest;
import nl.altindag.crip.command.TestServer;
import nl.altindag.log.LogCaptor;
import nl.altindag.ssl.server.service.Server;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nl.altindag.crip.IOTestUtils.getResourceContent;
import static org.assertj.core.api.Assertions.assertThat;

class PemExportRequestShould extends FileBaseTest {

    @Test
    void exportMultipleCertificateFromChainAsIndividualFiles() throws IOException {
        List<String> expectedCertificates = Arrays.asList(
                getResourceContent("reference-files/pem/server-one/cn=certificate-ripper-server-one_ou=amsterdam_o=thunderberry_c=nl_with_header.crt"),
                getResourceContent("reference-files/pem/server-one/cn=root-ca_ou=certificate-authority_o=thunderberry_c=nl_with_header.crt")
        );

        assertThat(expectedCertificates).isNotEmpty();

        PemExportRequest request = CertificateRipper.forExportingToPem("https://localhost:8443");
        request.setDestination(TEMP_DIRECTORY.toAbsolutePath());
        request.run();

        assertThat(consoleCaptor.getStandardOutput()).contains("Extracted 2 certificates.");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files)
                .hasSize(expectedCertificates.size())
                .allMatch(path -> path.toString().endsWith(".crt"));

        for (Path file : files) {
            String content = Files.lines(file).collect(Collectors.joining(System.lineSeparator()));
            assertThat(expectedCertificates).contains(content);
        }
    }

    @Test
    void exportMultipleCertificateFromChainAsSingleCombinedFile() throws IOException {
        String expectedCertificate = getResourceContent("reference-files/pem/server-one/cn=certificate-ripper-server-one_ou=amsterdam_o=thunderberry_c=nl_with_header_combined.crt");
        assertThat(expectedCertificate).isNotEmpty();

        PemExportRequest request = CertificateRipper.forExportingToPem("https://localhost:8443");
        request.setCombined(true);
        request.setDestination(TEMP_DIRECTORY.toAbsolutePath().resolve("localhost.crt"));
        request.run();

        assertThat(consoleCaptor.getStandardOutput()).contains("Extracted 2 certificates.");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files)
                .hasSize(1)
                .allMatch(path -> path.toString().endsWith(".crt"));

        String content = Files.lines(files.get(0)).collect(Collectors.joining(System.lineSeparator()));
        assertThat(content).isEqualTo(expectedCertificate);
    }

    @Test
    void exportMultipleCertificateFromChainAsSingleCombinedFileWithMultipleUrls() throws IOException {
        Map<String, String> expectedCertificatesAsPem = new HashMap<>();
        expectedCertificatesAsPem.put("localhost-1", getResourceContent("reference-files/pem/server-one/cn=certificate-ripper-server-one_ou=amsterdam_o=thunderberry_c=nl_with_header_combined.crt"));
        expectedCertificatesAsPem.put("localhost-2", getResourceContent("reference-files/pem/server-two/cn=certificate-ripper-server-two_ou=amsterdam_o=thunderberry_c=nl_with_header_combined.crt"));

        PemExportRequest request = CertificateRipper.forExportingToPem(List.of("https://localhost:8443", "https://localhost:8444"));
        request.setCombined(true);
        request.setDestination(TEMP_DIRECTORY.toAbsolutePath());
        request.run();

        assertThat(consoleCaptor.getStandardOutput()).contains("Extracted 4 certificates.");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files)
                .hasSize(2)
                .allMatch(path -> path.toString().endsWith(".crt"));

        for (Path file : files) {
            String content = Files.lines(file).collect(Collectors.joining(System.lineSeparator()));
            assertThat(expectedCertificatesAsPem).containsValue(content);
        }
    }

    @Test
    void exportMultipleCertificateFromChainAsIndividualFilesWithoutHeaders() throws IOException {
        List<String> expectedCertificates = Arrays.asList(
                getResourceContent("reference-files/pem/server-one/cn=certificate-ripper-server-one_ou=amsterdam_o=thunderberry_c=nl_without_header.crt"),
                getResourceContent("reference-files/pem/server-one/cn=root-ca_ou=certificate-authority_o=thunderberry_c=nl_without_header.crt")
        );

        assertThat(expectedCertificates).isNotEmpty();

        PemExportRequest request = CertificateRipper.forExportingToPem("https://localhost:8443");
        request.setIncludeHeader(false);
        request.setDestination(TEMP_DIRECTORY.toAbsolutePath());
        request.run();

        assertThat(consoleCaptor.getStandardOutput()).contains("Extracted 2 certificates.");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files).allMatch(path -> path.toString().endsWith(".crt"));

        for (Path file : files) {
            String content = Files.lines(file).collect(Collectors.joining(System.lineSeparator()));
            assertThat(expectedCertificates).contains(content);
        }
    }

    @Test
    void exportMultipleCertificateFromChainAsSingleCombinedFileWithoutHeaders() throws IOException {
        String expectedCertificate = getResourceContent("reference-files/pem/server-one/cn=certificate-ripper-server-one_ou=amsterdam_o=thunderberry_c=nl_without_header_combined.crt");

        assertThat(expectedCertificate).isNotEmpty();

        PemExportRequest request = CertificateRipper.forExportingToPem("https://localhost:8443");
        request.setIncludeHeader(false);
        request.setCombined(true);
        request.setDestination(TEMP_DIRECTORY.toAbsolutePath().resolve("thunderberry.crt").toString());
        request.run();

        assertThat(consoleCaptor.getStandardOutput()).contains("Extracted 2 certificates.");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files)
                .hasSize(1)
                .allMatch(path -> path.toString().endsWith(".crt"));

        String content = Files.lines(files.get(0)).collect(Collectors.joining(System.lineSeparator()));
        assertThat(content).isEqualTo(expectedCertificate);
    }

    @Test
    void timeoutWhenServerTakesToLongToRespond() throws IOException {
        LogCaptor logCaptor = LogCaptor.forRoot();
        Server server = Server.builder(TestServer.getInstance().getSslFactoryForServerOne())
                .withPort(8446)
                .withDelayedResponseTime(500)
                .build();

        PemExportRequest request = CertificateRipper.forExportingToPem("https://localhost:8446");
        request.setDestination(TEMP_DIRECTORY.toAbsolutePath().resolve("thunderberry.crt"));
        request.setTimeoutInMilliseconds(250);
        request.run();

        assertThat(consoleCaptor.getStandardOutput())
                .contains(
                        "Certificate ripper statistics:",
                        "- Certificate count",
                        "  * 0: https://localhost:8446"
                );

        assertThat(logCaptor.getDebugLogs()).contains("The client didn't get a respond within the configured time-out of [250] milliseconds from: [https://localhost:8446]");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files).isEmpty();
        server.stop();
        logCaptor.close();
    }

    @Test
    @SuppressWarnings("UnusedLabel")
    void resolveRootCaOnlyWhenEnabled() throws IOException {
        resolvedRootCa:
        {
            PemExportRequest request = CertificateRipper.forExportingToPem("https://google.com");
            request.setDestination(TEMP_DIRECTORY.toAbsolutePath());
            request.setResolveRootCa(true);
            request.run();

            assertThat(consoleCaptor.getStandardOutput()).contains("Extracted 4 certificates.");

            List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            assertThat(files)
                    .hasSize(4)
                    .allMatch(path -> path.toString().endsWith(".crt"));
        }

        createTempDirAndClearConsoleCaptor();

        notResolvedRootCa:
        {
            PemExportRequest request = CertificateRipper.forExportingToPem("https://google.com");
            request.setDestination(TEMP_DIRECTORY.toAbsolutePath());
            request.setResolveRootCa(false);
            request.run();

            assertThat(consoleCaptor.getStandardOutput()).contains("Extracted 3 certificates.");

            List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            assertThat(files)
                    .hasSize(3)
                    .allMatch(path -> path.toString().endsWith(".crt"));
        }
    }

    @Test
    void processSystemTrustedCertificates() throws IOException {
        createTempDirAndClearConsoleCaptor();

        PemExportRequest request = CertificateRipper.forExportingToPem("system");
        request.setDestination(TEMP_DIRECTORY.toAbsolutePath());
        request.setCombined(true);
        request.run();

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files)
                .hasSize(1)
                .allMatch(path -> path.toString().endsWith(".crt"));
    }

}
