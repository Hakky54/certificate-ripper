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
package nl.altindag.crip.model;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CertificateHolder {

    private final Map<String, List<X509Certificate>> urlsToCertificates;
    private final List<X509Certificate> allCertificates;
    private final List<X509Certificate> uniqueCertificates;
    private final List<X509Certificate> duplicateCertificates;
    private final List<X509Certificate> expiredCertificates;

    public CertificateHolder(Map<String, List<X509Certificate>> urlsToCertificates) {
        List<X509Certificate> certificates = urlsToCertificates.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<X509Certificate> uniqueCerts = new ArrayList<>();
        List<X509Certificate> duplicateCerts = new ArrayList<>();

        for (X509Certificate certificate : certificates) {
            if (!uniqueCerts.contains(certificate)) {
                uniqueCerts.add(certificate);
            } else {
                duplicateCerts.add(certificate);
            }
        }

        List<X509Certificate> expiredCerts = new ArrayList<>();
        Instant now = Instant.now();
        for (X509Certificate certificate : uniqueCerts) {
            Instant expirationDateTime = certificate.getNotAfter().toInstant();
            if (expirationDateTime.isBefore(now)) {
                expiredCerts.add(certificate);
            }
        }

        this.urlsToCertificates = Collections.unmodifiableMap(urlsToCertificates);
        this.allCertificates = Collections.unmodifiableList(certificates);
        this.uniqueCertificates = Collections.unmodifiableList(uniqueCerts);
        this.duplicateCertificates = Collections.unmodifiableList(duplicateCerts);
        this.expiredCertificates = Collections.unmodifiableList(expiredCerts);
    }

    public Map<String, List<X509Certificate>> getUrlsToCertificates() {
        return urlsToCertificates;
    }

    public List<X509Certificate> getAllCertificates() {
        return allCertificates;
    }

    public List<X509Certificate> getUniqueCertificates() {
        return uniqueCertificates;
    }

    public List<X509Certificate> getDuplicateCertificates() {
        return duplicateCertificates;
    }

    public List<X509Certificate> getExpiredCertificates() {
        return expiredCertificates;
    }

}
