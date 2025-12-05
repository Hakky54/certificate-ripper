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
package nl.altindag.crip.request.print;

import nl.altindag.crip.CertificateRipper;
import nl.altindag.crip.command.BaseTest;
import nl.altindag.ssl.util.CertificateUtils;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.altindag.crip.IOTestUtils.getResourceContent;
import static nl.altindag.crip.model.Format.PEM;
import static org.assertj.core.api.Assertions.assertThat;

class PrintCommandShould extends BaseTest {

    @Test
    void printUrlHeaderAndDelimiter() {
        List<Certificate> expectedCertificates = Stream.concat(
                        CertificateUtils.loadCertificate("reference-files/der/server-one/cn=certificate-ripper-server-one_ou=amsterdam_o=thunderberry_c=nl.crt").stream(),
                        CertificateUtils.loadCertificate("reference-files/der/server-one/cn=root-ca_ou=certificate-authority_o=thunderberry_c=nl.crt").stream())
                .collect(Collectors.toList());

        assertThat(expectedCertificates).hasSize(2);

        CertificateRipper.print("https://localhost:8443").run();

        assertThat(consoleCaptor.getStandardOutput()).areExactly(1, new Condition<>("Certificates for url = https://localhost:8443"::equals, null));
        assertThat(consoleCaptor.getStandardOutput()).areExactly(expectedCertificates.size() - 1, new Condition<>("<========== Next certificate for https://localhost:8443 ==========>"::equals, null));
    }

    @Test
    void printUrlHeaderMultipleTimesWhenMultipleUrlsArePresent() {
        List<Certificate> expectedCertificatesForServerOne = Stream.concat(
                        CertificateUtils.loadCertificate("reference-files/der/server-one/cn=certificate-ripper-server-one_ou=amsterdam_o=thunderberry_c=nl.crt").stream(),
                        CertificateUtils.loadCertificate("reference-files/der/server-one/cn=root-ca_ou=certificate-authority_o=thunderberry_c=nl.crt").stream())
                .collect(Collectors.toList());
        assertThat(expectedCertificatesForServerOne).hasSize(2);

        List<Certificate> expectedCertificatesForServerTwo = Stream.concat(
                        CertificateUtils.loadCertificate("reference-files/der/server-two/cn=certificate-ripper-server-two_ou=amsterdam_o=thunderberry_c=nl.crt").stream(),
                        CertificateUtils.loadCertificate("reference-files/der/server-two/cn=root-ca_ou=certificate-authority_o=thunderberry_c=nl.crt").stream())
                .collect(Collectors.toList());
        assertThat(expectedCertificatesForServerTwo).hasSize(2);

        CertificateRipper.print(List.of("https://localhost:8443", "https://localhost:8444")).run();

        assertThat(consoleCaptor.getStandardOutput()).areExactly(1, new Condition<>("Certificates for url = https://localhost:8443"::equals, null));
        assertThat(consoleCaptor.getStandardOutput()).areExactly(1, new Condition<>("Certificates for url = https://localhost:8444"::equals, null));
        assertThat(consoleCaptor.getStandardOutput()).areExactly(expectedCertificatesForServerOne.size() - 1, new Condition<>("<========== Next certificate for https://localhost:8443 ==========>"::equals, null));
        assertThat(consoleCaptor.getStandardOutput()).areExactly(expectedCertificatesForServerTwo.size() - 1, new Condition<>("<========== Next certificate for https://localhost:8444 ==========>"::equals, null));
    }

    @Test
    void printCertificateInX509Format() {
        List<Certificate> expectedCertificates = Stream.concat(
                        CertificateUtils.loadCertificate("reference-files/der/server-one/cn=certificate-ripper-server-one_ou=amsterdam_o=thunderberry_c=nl.crt").stream(),
                        CertificateUtils.loadCertificate("reference-files/der/server-one/cn=root-ca_ou=certificate-authority_o=thunderberry_c=nl.crt").stream())
                .collect(Collectors.toList());

        assertThat(expectedCertificates).hasSize(2);

        CertificateRipper.print("https://localhost:8443").run();

        String output = String.join(System.lineSeparator(), consoleCaptor.getStandardOutput());
        for (Certificate expectedCertificate : expectedCertificates) {
            assertThat(output).contains(expectedCertificate.toString());
        }
    }

    @Test
    void printCertificateInPemFormat() {
        List<String> expectedCertificates = Arrays.asList(
                getResourceContent("reference-files/pem/server-one/cn=certificate-ripper-server-one_ou=amsterdam_o=thunderberry_c=nl_with_header.crt"),
                getResourceContent("reference-files/pem/server-one/cn=root-ca_ou=certificate-authority_o=thunderberry_c=nl_with_header.crt")
        );

        assertThat(expectedCertificates).isNotEmpty();

        PrintRequest request = CertificateRipper.print("https://localhost:8443");
        request.setFormat(PEM);
        request.run();

        String output = String.join(System.lineSeparator(), consoleCaptor.getStandardOutput());
        for (String expectedCertificate : expectedCertificates) {
            assertThat(output).contains(expectedCertificate);
        }
    }

}
