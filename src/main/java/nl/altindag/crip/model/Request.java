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

import nl.altindag.crip.CertificateRipper;

import java.util.Arrays;
import java.util.List;

public abstract class Request implements Runnable {

    private final List<String> urls;
    private ProxyOptions proxyOptions;
    private Integer timeoutInMilliseconds;
    private Boolean resolveRootCa;
    private CertificateType certificateType;

    public Request(List<String> urls) {
        this.urls = urls;
    }

    @Override
    public void run() {
        String[] arguments = Arrays.stream(this.toString().split(" "))
                .map(String::trim)
                .filter(argument -> !argument.isEmpty())
                .toList()
                .toArray(new String[0]);

        CertificateRipper.main(arguments);
    }

    public Request withProxyOptions(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    public Request withTimeoutInMilliseconds(Integer timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
        return this;
    }

    public Request withResolveRootCa(Boolean resolveRootCa) {
        this.resolveRootCa = resolveRootCa;
        return this;
    }

    public Request withCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
        return this;
    }

    public List<String> getUrls() {
        return urls;
    }

    @Override
    public String toString() {
        StringBuilder requestBuilder = new StringBuilder();
        urls.forEach(url -> requestBuilder.append(" --url=").append(url));
        if (proxyOptions != null) {
            requestBuilder.append(" ").append(proxyOptions);
        }
        if (timeoutInMilliseconds != null) {
            requestBuilder.append(" --timeout=").append(timeoutInMilliseconds);
        }
        if (resolveRootCa != null) {
            requestBuilder.append(" --resolve-ca=").append(resolveRootCa);
        }
        if (certificateType != null) {
            requestBuilder.append(" --cert-type=").append(certificateType);
        }

        return requestBuilder.toString();
    }

}
