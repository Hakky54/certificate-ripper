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

import nl.altindag.crip.model.CertificateHolder;
import nl.altindag.crip.util.TriFunction;
import nl.altindag.ssl.util.CertificateUtils;
import nl.altindag.ssl.util.internal.UriUtils;
import picocli.CommandLine.Option;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static nl.altindag.ssl.util.internal.StringUtils.isNotBlank;

@SuppressWarnings("unused")
public class SharedProperties {

    @Option(names = {"-u", "--url"}, description = "Url of the target server to extract the certificates", required = true)
    private String[] urls;
    private List<String> uniqueUrls;

    @Option(names = {"--proxy-host"}, description = "Proxy host")
    private String proxyHost;

    @Option(names = {"--proxy-port"}, description = "Proxy port")
    private Integer proxyPort;

    @Option(names = {"--proxy-user"}, description = "User for authenticating the user for the given proxy")
    private String proxyUser;

    @Option(names = {"--proxy-password"}, interactive = true, description = "Password for authenticating the user for the given proxy")
    private String proxyPassword;

    public CertificateHolder getCertificateHolder() {
        List<String> uniqueUrls = getUrls();

        Map<String, List<X509Certificate>> urlsToCertificates = getCertificates(uniqueUrls,
                CertificateUtils::getCertificatesFromExternalSources,
                CertificateUtils::getCertificatesFromExternalSources,
                CertificateUtils::getCertificatesFromExternalSources);

        return new CertificateHolder(urlsToCertificates);
    }

    public List<X509Certificate> getCertificatesFromFirstUrl() {
        return getCertificates(urls[0],
                CertificateUtils::getCertificatesFromExternalSource,
                CertificateUtils::getCertificatesFromExternalSource,
                CertificateUtils::getCertificatesFromExternalSource);
    }

    public List<String> getUrls() {
        if (uniqueUrls != null) {
            return uniqueUrls;
        }

        uniqueUrls = new ArrayList<>();
        Map<String, List<Integer>> hostToPort = new HashMap<>();

        for (String url : urls) {
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

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private <T, R> R getCertificates(T sourceProvider,
                                     Function<T, R> certificateExtractor,
                                     BiFunction<Proxy, T, R> certificateExtractorWithProxy,
                                     TriFunction<Proxy, PasswordAuthentication, T, R> certificateExtractorWithProxyAndAuthentication) {

        if (isNotBlank(proxyHost) && proxyPort != null && isNotBlank(proxyUser) && isNotBlank(proxyPassword)) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            PasswordAuthentication passwordAuthentication = new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
            return certificateExtractorWithProxyAndAuthentication.apply(proxy, passwordAuthentication, sourceProvider);
        }

        if (isNotBlank(proxyHost) && proxyPort != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            return certificateExtractorWithProxy.apply(proxy, sourceProvider);
        }

        return certificateExtractor.apply(sourceProvider);
    }

}
