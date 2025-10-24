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

import nl.altindag.crip.command.SharedProperties.CertificateType;
import nl.altindag.ssl.util.CertificateUtils;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SharedPropertiesShould {

    private static final String CertificatePath = "reference-files/der/google/";
    private static Map<String, List<X509Certificate>> urlsToCertificates;
    private SharedProperties victim = new SharedProperties();

    @BeforeAll
    static void loadCertificates() {
        List<Certificate> one = CertificateUtils.loadCertificate(CertificatePath + "1-google.crt");
        List<Certificate> two = CertificateUtils.loadCertificate(CertificatePath + "2-wr2.crt");
        List<Certificate> three = CertificateUtils.loadCertificate(CertificatePath + "3-gts-root-r1.crt");
        List<Certificate> four = CertificateUtils.loadCertificate(CertificatePath + "4-globalsign-root-ca.crt");

        List<X509Certificate> certificates = Stream.of(one, two, three, four)
                .flatMap(List::stream)
                .filter(X509Certificate.class::isInstance)
                .map(X509Certificate.class::cast)
                .toList();

        assertThat(certificates).hasSize(4);
        urlsToCertificates = Map.of("https://google.com", certificates);
    }

    @Test
    void doNotFilterCertificatesWhenAllCertificateTypeIsSpecified() {
        Map<String, List<X509Certificate>> filteredUrlsToCertificates = victim.filterCertificatesIfNeeded(urlsToCertificates, CertificateType.ALL);
        assertThat(filteredUrlsToCertificates).hasSize(1).hasKeySatisfying(new Condition<>("https://google.com"::equals, "Key is https://google.com"));
        assertThat(filteredUrlsToCertificates.get("https://google.com")).hasSize(4);
    }

    @Test
    void filterCertificatesWhenRootCertificateTypeIsSpecified() {
        Map<String, List<X509Certificate>> filteredUrlsToCertificates = victim.filterCertificatesIfNeeded(urlsToCertificates, CertificateType.ROOT);
        assertThat(filteredUrlsToCertificates).hasSize(1).hasKeySatisfying(new Condition<>("https://google.com"::equals, "Key is https://google.com"));
        assertThat(filteredUrlsToCertificates.get("https://google.com")).hasSize(1);

        Certificate certificate = CertificateUtils.loadCertificate(CertificatePath + "4-globalsign-root-ca.crt").getFirst();
        assertThat(filteredUrlsToCertificates.get("https://google.com").getFirst()).isEqualTo(certificate);
    }

    @Test
    void filterCertificatesWhenLeafCertificateTypeIsSpecified() {
        Map<String, List<X509Certificate>> filteredUrlsToCertificates = victim.filterCertificatesIfNeeded(urlsToCertificates, CertificateType.LEAF);
        assertThat(filteredUrlsToCertificates).hasSize(1).hasKeySatisfying(new Condition<>("https://google.com"::equals, "Key is https://google.com"));
        assertThat(filteredUrlsToCertificates.get("https://google.com")).hasSize(1);

        Certificate certificate = CertificateUtils.loadCertificate(CertificatePath + "1-google.crt").getFirst();
        assertThat(filteredUrlsToCertificates.get("https://google.com").getFirst()).isEqualTo(certificate);
    }

    @Test
    void filterCertificatesWhenInterCertificateTypeIsSpecified() {
        Map<String, List<X509Certificate>> filteredUrlsToCertificates = victim.filterCertificatesIfNeeded(urlsToCertificates, CertificateType.INTER);
        assertThat(filteredUrlsToCertificates).hasSize(1).hasKeySatisfying(new Condition<>("https://google.com"::equals, "Key is https://google.com"));
        assertThat(filteredUrlsToCertificates.get("https://google.com")).hasSize(2);

        X509Certificate one = (X509Certificate) CertificateUtils.loadCertificate(CertificatePath + "2-wr2.crt").getFirst();
        X509Certificate two = (X509Certificate) CertificateUtils.loadCertificate(CertificatePath + "3-gts-root-r1.crt").getFirst();
        assertThat(filteredUrlsToCertificates.get("https://google.com")).containsExactlyInAnyOrder(one, two);
    }

}
