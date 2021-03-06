package nl.altindag.ssl.command;

import nl.altindag.ssl.util.CertificateUtils;
import picocli.CommandLine.Option;

import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
class SharedProperties {

    @Option(names = {"-u", "--url"}, description = "Url of the target server to extract the certificates", required = true)
    private String[] urls;

    public Map<String, List<Certificate>> getUrlsToCertificates() {
        return CertificateUtils.getCertificate(urls);
    }

}
