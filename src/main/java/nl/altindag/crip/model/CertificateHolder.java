package nl.altindag.crip.model;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CertificateHolder {

    private final List<X509Certificate> allCertificates;
    private final List<X509Certificate> uniqueCertificates;
    private final List<X509Certificate> duplicateCertificates;

    public CertificateHolder(Map<String, List<X509Certificate>> urlsToCertificates) {
        List<X509Certificate> certificates = urlsToCertificates.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<X509Certificate> uniqueCertificates = new ArrayList<>();
        List<X509Certificate> duplicateCertificates = new ArrayList<>();

        for (X509Certificate certificate : certificates) {
            if (!uniqueCertificates.contains(certificate)) {
                uniqueCertificates.add(certificate);
            } else {
                duplicateCertificates.add(certificate);
            }
        }

        this.allCertificates = Collections.unmodifiableList(certificates);
        this.uniqueCertificates = Collections.unmodifiableList(uniqueCertificates);
        this.duplicateCertificates = Collections.unmodifiableList(duplicateCertificates);
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

}
