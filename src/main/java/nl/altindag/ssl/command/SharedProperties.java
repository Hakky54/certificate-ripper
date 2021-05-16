package nl.altindag.ssl.command;

import nl.altindag.ssl.util.CertificateUtils;
import picocli.CommandLine.Option;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SharedProperties {

    private final Map<String, List<Certificate>> urlsToCertificates = new HashMap<>();
    private final Set<String> urls = new HashSet<>();

    private boolean isExtracted = false;

    public Map<String, List<Certificate>> getUrlsToCertificates() {
        if (!isExtracted) {
            Map<String, List<Certificate>> certificate = CertificateUtils.getCertificate(new ArrayList<>(urls));
            urlsToCertificates.putAll(certificate);
            isExtracted = true;
        }
        return urlsToCertificates;
    }

    @Option(names = {"-u", "--url"}, description = "Url of the target server to extract the certificates", required = true)
    public void urls(String[] urls) {
        this.urls.addAll(Arrays.asList(urls));
    }

}
