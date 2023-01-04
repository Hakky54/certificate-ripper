package nl.altindag.crip.command;

import nl.altindag.console.ConsoleCaptor;
import nl.altindag.ssl.util.CertificateUtils;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PrintCommandShould {

    private static CommandLine cmd;
    private static ConsoleCaptor consoleCaptor;

    @BeforeAll
    static void setupCertificateRipperAndConsoleCaptor() {
        CertificateRipper certificateRipper = new CertificateRipper();
        cmd = new CommandLine(certificateRipper)
                .setCaseInsensitiveEnumValuesAllowed(true);
        consoleCaptor = new ConsoleCaptor();
    }

    @BeforeEach
    void clearConsoleCaptor() {
        consoleCaptor.clearOutput();
    }

    @Test
    void printUrlHeaderAndDelimiter() {
        List<X509Certificate> expectedCertificates = CertificateUtils.getCertificatesFromExternalSource("https://google.com");
        assertThat(expectedCertificates).isNotEmpty();

        cmd.execute("print", "-u=https://google.com");

        assertThat(consoleCaptor.getStandardOutput()).areExactly(1, new Condition<>("Certificates for url = https://google.com"::equals, null));
        assertThat(consoleCaptor.getStandardOutput()).areExactly(expectedCertificates.size() -1, new Condition<>("========== NEXT CERTIFICATE FOR https://google.com =========="::equals, null));
    }

    @Test
    void printUrlHeaderMultipleTimesWhenMultipleUrlsArePresent() {
        Map<String, List<X509Certificate>> expectedCertificates = CertificateUtils.getCertificatesFromExternalSources("https://google.com", "https://github.com");
        assertThat(expectedCertificates).isNotEmpty();

        cmd.execute("print", "-u=https://google.com", "-u=https://github.com");

        assertThat(consoleCaptor.getStandardOutput()).areExactly(1, new Condition<>("Certificates for url = https://google.com"::equals, null));
        assertThat(consoleCaptor.getStandardOutput()).areExactly(1, new Condition<>("Certificates for url = https://github.com"::equals, null));
        assertThat(consoleCaptor.getStandardOutput()).areExactly(expectedCertificates.get("https://google.com").size() -1, new Condition<>("========== NEXT CERTIFICATE FOR https://google.com =========="::equals, null));
        assertThat(consoleCaptor.getStandardOutput()).areExactly(expectedCertificates.get("https://github.com").size() -1, new Condition<>("========== NEXT CERTIFICATE FOR https://github.com =========="::equals, null));
    }

    @Test
    void printCertificateInX509Format() {
        List<X509Certificate> expectedCertificates = CertificateUtils.getCertificatesFromExternalSource("https://google.com");
        assertThat(expectedCertificates).isNotEmpty();

        cmd.execute("print", "-u=https://google.com");

        String output = String.join(System.lineSeparator(), consoleCaptor.getStandardOutput());
        for (X509Certificate expectedCertificate : expectedCertificates) {
            assertThat(output).contains(expectedCertificate.toString());
        }
    }

    @Test
    void printCertificateInPemFormat() {
        List<String> expectedCertificates = CertificateUtils.getCertificatesFromExternalSourceAsPem("https://google.com");
        assertThat(expectedCertificates).isNotEmpty();

        cmd.execute("print", "-u=https://google.com", "-f=pem");

        String output = String.join(System.lineSeparator(), consoleCaptor.getStandardOutput());
        for (String expectedCertificate : expectedCertificates) {
            assertThat(output).contains(expectedCertificate);
        }
    }

}
