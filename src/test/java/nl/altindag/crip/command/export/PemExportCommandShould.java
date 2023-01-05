package nl.altindag.crip.command.export;

import nl.altindag.console.ConsoleCaptor;
import nl.altindag.crip.command.CertificateRipper;
import nl.altindag.ssl.util.CertificateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class PemExportCommandShould {

    private static final Path TEMP_DIRECTORY = Paths.get(System.getProperty("user.home"), "certificate-ripper-temp");

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
    void CreateTempDirAndClearConsoleCaptor() throws IOException {
        if (Files.exists(TEMP_DIRECTORY)) {
            List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            for (Path file : files) {
                Files.delete(file);
            }

            Files.deleteIfExists(TEMP_DIRECTORY);
        }

        Files.createDirectories(TEMP_DIRECTORY);
        consoleCaptor.clearOutput();
    }

    @Test
    void exportMultipleCertificateFromChainAsIndividualFiles() throws IOException {
        List<X509Certificate> expectedCertificates = CertificateUtils.getCertificatesFromExternalSource("https://google.com");
        List<String> expectedCertificatesAsPem = expectedCertificates.stream()
                .map(CertificateUtils::convertToPem)
                .collect(Collectors.toList());

        assertThat(expectedCertificates).isNotEmpty();

        cmd.execute("export", "pem", "--url=https://google.com", "--destination=" + TEMP_DIRECTORY.toAbsolutePath());

        assertThat(consoleCaptor.getStandardOutput()).contains("Successfully Exported certificates");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files.size()).isEqualTo(expectedCertificates.size());
        assertThat(files).allMatch(path -> path.toString().endsWith(".crt"));

        for (Path file : files) {
            String content = Files.lines(file).collect(Collectors.joining(System.lineSeparator()));
            assertThat(expectedCertificatesAsPem).contains(content);
        }
    }

    @Test
    void exportMultipleCertificateFromChainAsSingleCombinedFile() throws IOException {
        List<X509Certificate> expectedCertificates = CertificateUtils.getCertificatesFromExternalSource("https://google.com");
        String expectedCertificatesAsPem = expectedCertificates.stream()
                .map(CertificateUtils::convertToPem)
                .collect(Collectors.joining(System.lineSeparator()));

        assertThat(expectedCertificates).isNotEmpty();

        cmd.execute("export", "pem", "--url=https://google.com", "--combined=true", "--destination=" + TEMP_DIRECTORY.toAbsolutePath().resolve("google.crt"));

        assertThat(consoleCaptor.getStandardOutput()).contains("Successfully Exported certificates");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files.size()).isNotEqualTo(expectedCertificates.size());
        assertThat(files.size()).isEqualTo(1);
        assertThat(files).allMatch(path -> path.toString().endsWith(".crt"));

        String content = Files.lines(files.get(0)).collect(Collectors.joining(System.lineSeparator()));
        assertThat(content).isEqualTo(expectedCertificatesAsPem);
    }

    @Test
    void exportMultipleCertificateFromChainAsSingleCombinedFileWithMultipleUrls() throws IOException {
        Map<String, List<X509Certificate>> expectedCertificates = CertificateUtils.getCertificatesFromExternalSources("https://google.com", "https://github.com");
        Map<String, String> expectedCertificatesAsPem = expectedCertificates.entrySet().stream()
                .map(entry -> new SimpleEntry<>(entry.getKey(), CertificateUtils.convertToPem(entry.getValue())))
                .map(entry -> new SimpleEntry<>(entry.getKey(), String.join(System.lineSeparator(), entry.getValue())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        assertThat(expectedCertificates).isNotEmpty();

        cmd.execute("export", "pem", "--url=https://google.com", "--url=https://github.com", "--combined=true", "--destination=" + TEMP_DIRECTORY.toAbsolutePath());

        assertThat(consoleCaptor.getStandardOutput()).contains("Successfully Exported certificates");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files.size()).isEqualTo(2);
        assertThat(files).allMatch(path -> path.toString().endsWith(".crt"));

        for (Path file : files) {
            String content = Files.lines(file).collect(Collectors.joining(System.lineSeparator()));
            assertThat(expectedCertificatesAsPem.values()).contains(content);
        }
    }

    @Test
    void exportMultipleCertificateFromChainAsIndividualFilesWithoutHeaders() throws IOException {
        List<X509Certificate> expectedCertificates = CertificateUtils.getCertificatesFromExternalSource("https://google.com");
        List<String> expectedCertificatesAsPem = expectedCertificates.stream()
                .map(CertificateUtils::convertToPem)
                .map(pem -> Stream.of(pem.split(System.lineSeparator())).skip(2).collect(Collectors.joining(System.lineSeparator())))
                .collect(Collectors.toList());

        assertThat(expectedCertificates).isNotEmpty();

        cmd.execute("export", "pem", "--url=https://google.com", "--destination=" + TEMP_DIRECTORY.toAbsolutePath(), "--pristine=true");

        assertThat(consoleCaptor.getStandardOutput()).contains("Successfully Exported certificates");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files.size()).isEqualTo(expectedCertificates.size());
        assertThat(files).allMatch(path -> path.toString().endsWith(".crt"));

        for (Path file : files) {
            String content = Files.lines(file).collect(Collectors.joining(System.lineSeparator()));
            assertThat(expectedCertificatesAsPem).contains(content);
        }
    }

    @Test
    void exportMultipleCertificateFromChainAsSingleCombinedFileWithoutHeaders() throws IOException {
        List<X509Certificate> expectedCertificates = CertificateUtils.getCertificatesFromExternalSource("https://google.com");
        String expectedCertificatesAsPem = expectedCertificates.stream()
                .map(CertificateUtils::convertToPem)
                .map(pem -> Stream.of(pem.split(System.lineSeparator())).skip(2).collect(Collectors.joining(System.lineSeparator())))
                .collect(Collectors.joining(System.lineSeparator()));

        assertThat(expectedCertificates).isNotEmpty();

        cmd.execute("export", "pem", "--url=https://google.com", "--combined=true", "--destination=" + TEMP_DIRECTORY.toAbsolutePath().resolve("google.crt"), "--pristine=true");

        assertThat(consoleCaptor.getStandardOutput()).contains("Successfully Exported certificates");

        List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        assertThat(files.size()).isNotEqualTo(expectedCertificates.size());
        assertThat(files.size()).isEqualTo(1);
        assertThat(files).allMatch(path -> path.toString().endsWith(".crt"));

        String content = Files.lines(files.get(0)).collect(Collectors.joining(System.lineSeparator()));
        assertThat(content).isEqualTo(expectedCertificatesAsPem);
    }

}
