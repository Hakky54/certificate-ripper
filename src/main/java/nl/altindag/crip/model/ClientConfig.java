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

import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "FieldCanBeLocal", "FieldMayBeFinal"})
public final class ClientConfig {

    @Option(names = {"-u", "--url"}, description = "Url of the target server to extract the certificates", required = true)
    private List<String> urls = new ArrayList<>();

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

    @Option(names = {"--cert-type"}, description = "To be extracted certificate types%nAvailable Formats: root, inter, leaf, all%nDefault: all")
    private CertificateType certificateType = CertificateType.ALL;

    @Option(names = {"--resolve-ca"}, description = "Indicator to automatically resolve the root ca%nPossible options: true, false")
    private Boolean resolveRootCa = true;

    @Option(names = {"--resolve-siblings"}, description = "Indicator to automatically resolve the certificates from DNS names%nPossible options: true, false")
    private Boolean resolveSiblings = false;

    public List<String> getUrls() {
        return urls;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public Integer getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }

    public CertificateType getCertificateType() {
        return certificateType;
    }

    public Boolean getResolveRootCa() {
        return resolveRootCa;
    }

    public Boolean getResolveSiblings() {
        return resolveSiblings;
    }

}
