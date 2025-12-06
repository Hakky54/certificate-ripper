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
package nl.altindag.crip.request;

import nl.altindag.crip.CertificateRipper;
import nl.altindag.crip.command.FileBaseTest;
import nl.altindag.crip.command.TestServer;
import nl.altindag.log.LogCaptor;
import nl.altindag.ssl.server.service.Server;
import nl.altindag.ssl.util.KeyStoreUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import static nl.altindag.crip.IOTestUtils.getResource;
import static org.assertj.core.api.Assertions.assertThat;

class JksExportRequestShould extends FileBaseTest {

    @Test
    void exportMultipleCertificateFromChainToACustomFilename() throws IOException, KeyStoreException {
        KeyStore expectedTruststore = KeyStoreUtils.loadKeyStore(getResource("reference-files/jks/server-one/truststore.jks"), "changeit".toCharArray());

        CertificateRipper.exportToJks("https://localhost:8443")
                .withDestination(TEMP_DIRECTORY.toAbsolutePath().resolve("my-truststore.jks"))
                .build()
                .run();

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files).hasSize(1);
        assertThat(consoleCaptor.getStandardOutput()).contains("Extracted 2 certificates.", "It has been exported to " + files.get(0));
        assertThat(files).allMatch(path -> path.toString().endsWith("my-truststore.jks"));

        KeyStore truststore = KeyStoreUtils.loadKeyStore(files.get(0), "changeit".toCharArray());

        int certificateCounter = 0;
        Enumeration<String> aliases = truststore.aliases();
        while (aliases.hasMoreElements()) {
            certificateCounter++;
            String alias = aliases.nextElement();
            Certificate certificate = truststore.getCertificate(alias);
            Certificate expectedCertificate = expectedTruststore.getCertificate(alias);
            assertThat(certificate).isEqualTo(expectedCertificate);
        }

        assertThat(certificateCounter).isEqualTo(2);
    }

    @Test
    void timeoutWhenServerTakesToLongToRespond() throws IOException {
        LogCaptor logCaptor = LogCaptor.forRoot();
        Server server = Server.builder(TestServer.getInstance().getSslFactoryForServerOne())
                .withPort(8448)
                .withDelayedResponseTime(500)
                .build();

        CertificateRipper.exportToJks(List.of("https://localhost:8448"))
                .withDestination(TEMP_DIRECTORY.toAbsolutePath().resolve("my-truststore.jks"))
                .withTimeoutInMilliseconds(250)
                .build()
                .run();

        assertThat(consoleCaptor.getStandardOutput())
                .contains(
                        "Certificate ripper statistics:",
                        "- Certificate count",
                        "  * 0: https://localhost:8448"
                );

        assertThat(logCaptor.getDebugLogs()).contains("The client didn't get a respond within the configured time-out of [250] milliseconds from: [https://localhost:8448]");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files).isEmpty();
        server.stop();
        logCaptor.close();
    }

    @Test
    void processSystemTrustedCertificates() throws IOException {
        createTempDirAndClearConsoleCaptor();

        CertificateRipper.exportToJks("system")
                .withDestination(TEMP_DIRECTORY.toAbsolutePath().resolve("my-truststore.jks"))
                .build()
                .run();

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files)
                .hasSize(1)
                .allMatch(path -> path.toString().endsWith(".jks"));
    }

}
