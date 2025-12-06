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
package nl.altindag.crip;

import nl.altindag.crip.command.CripCommand;
import nl.altindag.crip.request.DerExportRequest;
import nl.altindag.crip.request.JksExportRequest;
import nl.altindag.crip.request.KeystoreExportRequest;
import nl.altindag.crip.request.PemExportRequest;
import nl.altindag.crip.request.Pkcs12ExportRequest;
import nl.altindag.crip.request.PrintRequest;
import nl.altindag.crip.provider.CertificateRipperProvider;
import nl.altindag.crip.util.HelpFactory;
import picocli.CommandLine;

import java.security.Security;
import java.util.Arrays;
import java.util.List;

public final class CertificateRipper {

    private CertificateRipper() {}

    public static void main(String[] applicationArguments) {
        // Temporally ignoring KeychainStore as it does not work with Graal VM yet.
        // The actual call to get the KeychainStore from the Apple Provider will be intercepted, and it will return a dummy keystore
        // See here for the related issue https://github.com/oracle/graal/issues/10387
        Security.insertProviderAt(new CertificateRipperProvider(), 1);

        new CommandLine(new CripCommand())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setHelpFactory(new HelpFactory())
                .execute(applicationArguments);
    }

    public static PrintRequest.Builder print(String... urls) {
        return PrintRequest.builder(Arrays.asList(urls));
    }

    public static PrintRequest.Builder print(List<String> urls) {
        return PrintRequest.builder(urls);
    }

    public static PemExportRequest.Builder exportToPem(String... urls) {
        return PemExportRequest.builder(Arrays.asList(urls));
    }

    public static PemExportRequest.Builder exportToPem(List<String> urls) {
        return PemExportRequest.builder(urls);
    }

    public static DerExportRequest.Builder exportToDer(String... urls) {
        return DerExportRequest.builder(Arrays.asList(urls));
    }

    public static DerExportRequest.Builder exportToDer(List<String> urls) {
        return DerExportRequest.builder(urls);
    }

    public static KeystoreExportRequest.Builder exportToPkcs12(String... urls) {
        return Pkcs12ExportRequest.builder(Arrays.asList(urls));
    }

    public static KeystoreExportRequest.Builder exportToPkcs12(List<String> urls) {
        return Pkcs12ExportRequest.builder(urls);
    }

    public static KeystoreExportRequest.Builder exportToJks(String... urls) {
        return JksExportRequest.builder(Arrays.asList(urls));
    }

    public static KeystoreExportRequest.Builder exportToJks(List<String> urls) {
        return JksExportRequest.builder(urls);
    }

}
