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
import nl.altindag.crip.model.ExportMode;

import java.nio.file.Path;
import java.util.List;

import static nl.altindag.crip.model.ExportMode.PEM;

public final class PemExportRequest extends CombineableExportRequest {

    private Boolean includeHeader = true;

    PemExportRequest(List<String> urls) {
        super(urls);
    }

    void setIncludeHeader(boolean includeHeader) {
        this.includeHeader = includeHeader;
    }

    @Override
    ExportMode getExportMode() {
        return PEM;
    }

    @Override
    public String toString() {
        return String.format("%s --include-header=%s", super.toString(), includeHeader);
    }

    public static Builder builder(List<String> urls) {
        return new Builder(urls);
    }

    public static final class Builder {

        private final List<String> urls;
        private String destination;
        private Boolean combined = false;
        private Boolean includeHeader = true;

        private ProxyOptions proxyOptions;
        private Integer timeoutInMilliseconds;
        private Boolean resolveRootCa;
        private Boolean resolveSiblings;
        private CertificateType certificateType;

        Builder(List<String> urls) {
            this.urls = urls;
        }

        public Builder withDestination(String destination) {
            this.destination = destination;
            return this;
        }

        public Builder withDestination(Path destination) {
            this.destination = destination.toString();
            return this;
        }

        public Builder withCombined(Boolean combined) {
            this.combined = combined;
            return this;
        }

        public Builder withIncludeHeader(Boolean includeHeader) {
            this.includeHeader = includeHeader;
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

        public Builder withResolveSiblings(Boolean resolveSiblings) {
            this.resolveSiblings = resolveSiblings;
            return this;
        }

        public Builder withCertificateType(CertificateType certificateType) {
            this.certificateType = certificateType;
            return this;
        }

        public Request build() {
            PemExportRequest request = new PemExportRequest(urls);
            request.setDestination(destination);
            request.setCombined(combined);
            request.setIncludeHeader(includeHeader);
            request.setProxyOptions(proxyOptions);
            request.setTimeoutInMilliseconds(timeoutInMilliseconds);
            request.setResolveRootCa(resolveRootCa);
            request.setResolveSiblings(resolveSiblings);
            request.setCertificateType(certificateType);
            return request;
        }

    }

}
