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
package nl.altindag.crip.request;

import nl.altindag.crip.model.CertificateType;
import nl.altindag.crip.model.Format;

import java.util.List;

import static nl.altindag.crip.model.Format.X509;

public final class PrintRequest extends Request {

    private Format format = X509;

    PrintRequest(List<String> urls) {
        super(urls);
    }

    void setFormat(Format format) {
        this.format = format;
    }

    @Override
    public String toString() {
        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append("print").append(" ")
                .append("--format=").append(format).append(" ");

        getUrls().forEach(url -> requestBuilder.append("--url=").append(url).append(" "));
        return requestBuilder.append(super.toString()).toString();
    }

    public static Builder builder(List<String> urls) {
        return new Builder(urls);
    }

    public static final class Builder {

        private final List<String> urls;
        private Format format = X509;

        private ProxyOptions proxyOptions;
        private Integer timeoutInMilliseconds;
        private Boolean resolveRootCa;
        private CertificateType certificateType;

        Builder(List<String> urls) {
            this.urls = urls;
        }

        public Builder withFormat(Format format) {
            this.format = format;
            return this;
        }

        public Builder withProxyOptions(ProxyOptions proxyOptions) {
            this.proxyOptions = proxyOptions;
            return this;
        }

        public Builder withTimeoutInMilliseconds(Integer timeoutInMilliseconds) {
            this.timeoutInMilliseconds = timeoutInMilliseconds;
            return this;
        }

        public Builder withResolveRootCa(Boolean resolveRootCa) {
            this.resolveRootCa = resolveRootCa;
            return this;
        }

        public Builder withCertificateType(CertificateType certificateType) {
            this.certificateType = certificateType;
            return this;
        }

        public Request build() {
            PrintRequest request = new PrintRequest(urls);
            request.setFormat(format);
            request.setProxyOptions(proxyOptions);
            request.setTimeoutInMilliseconds(timeoutInMilliseconds);
            request.setResolveRootCa(resolveRootCa);
            request.setCertificateType(certificateType);
            return request;
        }

    }

}
