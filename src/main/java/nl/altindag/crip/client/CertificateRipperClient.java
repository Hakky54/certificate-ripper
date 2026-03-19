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
package nl.altindag.crip.client;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import nl.altindag.crip.client.ftp.FtpsClientRunnable;
import nl.altindag.crip.client.imap.ImapClientRunnable;
import nl.altindag.crip.client.mysql.MySQLClientRunnable;
import nl.altindag.crip.client.postgres.PostgresClientRunnable;
import nl.altindag.crip.client.smtp.SmtpClientRunnable;
import nl.altindag.crip.client.websocket.WebSocketClientRunnable;
import nl.altindag.crip.model.ClientConfig;
import nl.altindag.crip.model.CertificateHolder;
import nl.altindag.crip.model.CertificateType;
import nl.altindag.crip.util.UriUtils;
import nl.altindag.ssl.util.CertificateExtractingClient;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import nl.altindag.ssl.util.CertificateUtils;
import nl.altindag.ssl.util.ClientRunnable;
import nl.altindag.sude.Logger;
import nl.altindag.sude.LoggerFactory;

import static nl.altindag.crip.util.StringUtils.isNotBlank;

public class CertificateRipperClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateRipperClient.class);
    private static final String SYSTEM = "system";

    private final ClientConfig clientConfig;

    public CertificateRipperClient(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public CertificateHolder getCertificateHolder() {
        List<String> resolvedUrls = getUniqueUrls(clientConfig.getUrls());
        Map<String, List<X509Certificate>> certificates = getCertificates(resolvedUrls);
        Map<String, List<X509Certificate>> siblings = getSiblings(certificates);
        Map<String, List<X509Certificate>> systemCertificates = getSystemCertificates();

        Map<String, List<X509Certificate>> urlsToCertificates = mergeMaps(certificates, siblings, systemCertificates);
        urlsToCertificates = filterCertificates(urlsToCertificates, clientConfig.getCertificateType());

        return new CertificateHolder(urlsToCertificates);
    }

    private Map<String, List<X509Certificate>> getCertificates(List<String> urls) {
        return urls.stream().parallel()
                .map(url -> {
                    try {
                        var client = createClient(url);
                        List<X509Certificate> certificates = client.get(url);
                        return Map.entry(url, certificates);
                    } catch (Exception e) {
                        LOGGER.debug(String.format("Could not extract from %s", url), e);
                        return null;
                    }})
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue), HashMap::new));
    }

    private CertificateExtractingClient createClient(String url) {
        ClientRunnable clientRunnable = switch (URI.create(url).getScheme()) {
            case "wss" -> WebSocketClientRunnable.getInstance();
            case "ftps" -> FtpsClientRunnable.getInstance();
            case "smtps" -> SmtpClientRunnable.getInstance();
            case "imaps" -> ImapClientRunnable.getInstance();
            case "postgresql" -> PostgresClientRunnable.getInstance();
            case "mysql" -> MySQLClientRunnable.getInstance();
            default -> null;
        };

        return createClient()
                .withClientRunnable(clientRunnable)
                .build();
    }

    private CertificateExtractingClient.Builder createClient() {
        CertificateExtractingClient.Builder clientBuilder = CertificateExtractingClient.builder().withResolvedRootCa(clientConfig.getResolveRootCa());

        if (isNotBlank(clientConfig.getProxyHost()) && clientConfig.getProxyPort() != null) {
            clientBuilder.withProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(clientConfig.getProxyHost(), clientConfig.getProxyPort())));
        }
        if (isNotBlank(clientConfig.getProxyUser()) && isNotBlank(clientConfig.getProxyPassword())) {
            clientBuilder.withPasswordAuthentication(new PasswordAuthentication(clientConfig.getProxyUser(), clientConfig.getProxyPassword().toCharArray()));
        }

        if (clientConfig.getTimeoutInMilliseconds() != null && clientConfig.getTimeoutInMilliseconds() > 0) {
            Duration timeout = Duration.of(clientConfig.getTimeoutInMilliseconds(), ChronoUnit.MILLIS);
            clientBuilder.withTimeout(timeout);
        }

        return clientBuilder;
    }

    private List<String> getUniqueUrls(List<String> urls) {
        List<String> uniqueUrls = new ArrayList<>();
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

    private Map<String, List<X509Certificate>> getSiblings(Map<String, List<X509Certificate>> urlsToCertificates) {
        if (!clientConfig.getResolveSiblings()) {
            return Collections.emptyMap();
        }

        ProgressBarBuilder pbb = new ProgressBarBuilder()
                .hideEta()
                .continuousUpdate()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setTaskName("Resolving sibling certificates").showSpeed();

        List<String> urls = urlsToCertificates.values().stream()
                .flatMap(certificates -> UriUtils.extractHostsFromSAN(certificates).stream())
                .distinct()
                .toList();

        CertificateExtractingClient client = createClient().build();
        ProgressBar.wrap(urls.stream(), pbb)
                .parallel()
                .forEach(url -> {
                    try {
                        client.call(url);
                    } catch (Exception e) {
                        LOGGER.debug(String.format("Could not extract sibling certificate from %s", url), e);
                    }
                });

        return client.getCertificatesCollector().entrySet().stream()
                .map(entry -> Map.entry("https://" + entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, List<X509Certificate>> getSystemCertificates() {
        if (clientConfig.getUrls().contains(SYSTEM)) {
            try {
                List<X509Certificate> systemTrustedCertificates = CertificateUtils.getSystemTrustedCertificates();
                return Map.of(SYSTEM, systemTrustedCertificates);
            } catch (UnsatisfiedLinkError error) {
                LOGGER.debug(String.format("Unable to extract system certificates for %s", System.getProperty("os.name")));
            }
        }
        return Collections.emptyMap();
    }

    Map<String, List<X509Certificate>> filterCertificates(Map<String, List<X509Certificate>> urlsToCertificates, CertificateType type) {
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

    @SafeVarargs
    private static <T, U> Map<T, U> mergeMaps(Map<T, U>... maps) {
        Map<T, U> mergedMap = new HashMap<>();
        for (Map<T, U> map : maps) {
            mergedMap.putAll(map);
        }
        return mergedMap;
    }

}
