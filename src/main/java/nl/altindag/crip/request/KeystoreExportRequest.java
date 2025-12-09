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

public abstract class KeystoreExportRequest extends ExportRequest {

    private String password;

    public KeystoreExportRequest(List<String> urls) {
        super(urls);
    }

    void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        if (password == null) {
            return super.toString();
        }

        return super.toString() + " --password=" + password;
    }

    public static final class Builder {

        private final List<String> urls;
        private final ExportMode exportMode;
        private String password;
        private String destination;

        private ProxyOptions proxyOptions;
        private Integer timeoutInMilliseconds;
        private Boolean resolveRootCa;
        private Boolean resolveSiblings;
        private CertificateType certificateType;

        Builder(List<String> urls, ExportMode exportMode) {
            this.urls = urls;
            this.exportMode = exportMode;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withDestination(String destination) {
            this.destination = destination;
            return this;
        }

        public Builder withDestination(Path destination) {
            this.destination = destination.toString();
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
            KeystoreExportRequest request = switch (exportMode) {
                case JKS -> new JksExportRequest(urls);
                case PKCS12 -> new Pkcs12ExportRequest(urls);
                default -> throw new IllegalStateException("Unexpected value: " + exportMode);
            };

            request.setPassword(password);
            request.setDestination(destination);
            request.setProxyOptions(proxyOptions);
            request.setTimeoutInMilliseconds(timeoutInMilliseconds);
            request.setResolveRootCa(resolveRootCa);
            request.setResolveSiblings(resolveSiblings);
            request.setCertificateType(certificateType);
            return request;
        }

    }
}
