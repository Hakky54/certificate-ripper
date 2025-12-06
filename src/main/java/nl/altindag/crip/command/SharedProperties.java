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

import nl.altindag.crip.client.ftp.FtpsClientRunnable;
import nl.altindag.crip.client.imap.ImapClientRunnable;
import nl.altindag.crip.client.smtp.SmtpClientRunnable;
import nl.altindag.crip.client.websocket.WebSocketClientRunnable;
import nl.altindag.crip.model.CertificateHolder;
import nl.altindag.crip.model.CertificateType;
import nl.altindag.ssl.util.CertificateExtractingClient;
import nl.altindag.ssl.util.CertificateUtils;
import nl.altindag.ssl.util.internal.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Option;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static nl.altindag.ssl.util.internal.StringUtils.isNotBlank;

@SuppressWarnings({"unused", "FieldCanBeLocal", "FieldMayBeFinal"})
public class SharedProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(SharedProperties.class);

    private static final String SYSTEM = "system";

    @Option(names = {"-u", "--url"}, description = "Url of the target server to extract the certificates", required = true)
    private List<String> urls = new ArrayList<>();
    private List<String> uniqueUrls;

    @Option(names = {"--proxy-host"}, description = "Proxy host")
    private String proxyHost;

    @Option(names = {"--proxy-port"}, description = "Proxy port")
    private Integer proxyPort;

    @Option(names = {"--proxy-user"}, description = "User for authenticating the user for the given proxy")
    private String proxyUser;

    @Option(names = {"--proxy-password"}, interactive = true, description = "Password for authenticating the user for the given proxy")
    private String proxyPassword;

    @Option(names = {"-t", "--timeout"}, description = "Amount of milliseconds till the ripping should timeout")
    private Integer timeoutInMilliseconds;

    @Option(names = {"--resolve-ca"}, description = "Indicator to automatically resolve the root ca%nPossible options: true, false")
    private Boolean resolveRootCa = true;

    @Option(names = {"--cert-type"},
            description = "To be extracted certificate types%nAvailable Formats: root, inter, leaf, all%nDefault: all")
    private CertificateType certificateType = CertificateType.ALL;

    public CertificateHolder getCertificateHolder() {
        List<String> resolvedUrls = getUrls();

        Map<String, List<X509Certificate>> urlsToCertificates = resolvedUrls.stream()
                .distinct()
                .parallel()
                .map(this::getCertificates)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.collectingAndThen(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (key1, key2) -> key1, LinkedHashMap::new), HashMap::new));

        urlsToCertificates = filterCertificatesIfNeeded(urlsToCertificates, certificateType);

        if (urls.contains(SYSTEM)) {
            try {
                List<X509Certificate> systemTrustedCertificates = CertificateUtils.getSystemTrustedCertificates();
                urlsToCertificates.put(SYSTEM, systemTrustedCertificates);
            } catch (UnsatisfiedLinkError error) {
                LOGGER.debug("Unable to extract system certificates for {}", System.getProperty("os.name"));
            }
        }

        return new CertificateHolder(urlsToCertificates);
    }

    private List<String> getUrls() {
        if (uniqueUrls != null) {
            return uniqueUrls;
        }

        uniqueUrls = new ArrayList<>();
        Map<String, List<Integer>> hostToPort = new HashMap<>();

        for (String url : urls) {
            if (SYSTEM.equals(url)) {
                continue;
            }

            String host = UriUtils.extractHost(url);
            int port = UriUtils.extractPort(url);

            if (hostToPort.containsKey(host)) {
                List<Integer> ports = hostToPort.get(host);
                if (ports.contains(port)) {
                    continue;
                }
            }

            List<Integer> ports = hostToPort.getOrDefault(host, new ArrayList<>());
            ports.add(port);

            hostToPort.put(host, ports);
            uniqueUrls.add(url);
        }
        return uniqueUrls;
    }

    private Optional<Map.Entry<String, List<X509Certificate>>> getCertificates(String url) {
        try {
            CertificateExtractingClient client = createClient(url);
            List<X509Certificate> certificates = client.get(url);
            return Optional.of(Map.entry(url, certificates));
        } catch (Exception e) {
            LOGGER.debug(String.format("Could not extract from %s", url), e);
            return Optional.empty();
        }
    }

    private CertificateExtractingClient createClient(String url) {
        CertificateExtractingClient.Builder clientBuilder = CertificateExtractingClient.builder().withResolvedRootCa(resolveRootCa);

        URI uri = URI.create(url);
        switch (uri.getScheme()) {
            case "wss" -> clientBuilder.withClientRunnable(new WebSocketClientRunnable());
            case "ftps" -> clientBuilder.withClientRunnable(new FtpsClientRunnable());
            case "smtps" -> clientBuilder.withClientRunnable(new SmtpClientRunnable());
            case "imaps" -> clientBuilder.withClientRunnable(new ImapClientRunnable());
            default -> {}
        }

        if (isNotBlank(proxyHost) && proxyPort != null) {
            clientBuilder.withProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }
        if (isNotBlank(proxyUser) && isNotBlank(proxyPassword)) {
            clientBuilder.withPasswordAuthentication(new PasswordAuthentication(proxyUser, proxyPassword.toCharArray()));
        }

        if (timeoutInMilliseconds != null && timeoutInMilliseconds > 0) {
            Duration timeout = Duration.of(timeoutInMilliseconds, ChronoUnit.MILLIS);
            clientBuilder.withTimeout(timeout);
        }

        return clientBuilder.build();
    }

    Map<String, List<X509Certificate>> filterCertificatesIfNeeded(Map<String, List<X509Certificate>> urlsToCertificates, CertificateType type) {
        return switch (type) {
            case ALL -> urlsToCertificates;
            case LEAF -> filterCertificates(urlsToCertificates, certificates -> List.of(certificates.getFirst()));
            case ROOT -> filterCertificates(urlsToCertificates, certificates -> List.of(certificates.getLast()));
            case INTER -> filterCertificates(urlsToCertificates, certificates -> {
                List<X509Certificate> intermediateCertificates = new ArrayList<>(certificates);
                intermediateCertificates.removeFirst();
                X509Certificate last = intermediateCertificates.getLast();
                if (CertificateUtils.isSelfSigned(last)) {
                    intermediateCertificates.removeLast();
                }
                return intermediateCertificates;
            });
        };
    }

    private Map<String, List<X509Certificate>> filterCertificates(Map<String, List<X509Certificate>> urlsToCertificates, UnaryOperator<List<X509Certificate>> valueMapper) {
        return urlsToCertificates.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), valueMapper.apply(entry.getValue())))
                .collect(Collectors.collectingAndThen(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue), HashMap::new));
    }

}
