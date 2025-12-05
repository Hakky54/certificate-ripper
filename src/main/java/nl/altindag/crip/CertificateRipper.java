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
import nl.altindag.crip.model.export.DerExportRequest;
import nl.altindag.crip.model.export.JksExportRequest;
import nl.altindag.crip.model.export.PemExportRequest;
import nl.altindag.crip.model.export.Pkcs12ExportRequest;
import nl.altindag.crip.model.print.PrintRequest;
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

    public static PrintRequest forPrinting(String... urls) {
        return new PrintRequest(Arrays.asList(urls));
    }

    public static PrintRequest forPrinting(List<String> urls) {
        return new PrintRequest(urls);
    }

    public static PemExportRequest forExportingToPem(String... urls) {
        return new PemExportRequest(Arrays.asList(urls));
    }

    public static PemExportRequest forExportingToPem(List<String> urls) {
        return new PemExportRequest(urls);
    }

    public static DerExportRequest forExportingToDer(String... urls) {
        return new DerExportRequest(Arrays.asList(urls));
    }

    public static DerExportRequest forExportingToDer(List<String> urls) {
        return new DerExportRequest(urls);
    }

    public static Pkcs12ExportRequest forExportingToPkcs12(String... urls) {
        return new Pkcs12ExportRequest(Arrays.asList(urls));
    }

    public static Pkcs12ExportRequest forExportingToPkcs12(List<String> urls) {
        return new Pkcs12ExportRequest(urls);
    }

    public static JksExportRequest forExportingToJks(String... urls) {
        return new JksExportRequest(Arrays.asList(urls));
    }

    public static JksExportRequest forExportingToJks(List<String> urls) {
        return new JksExportRequest(urls);
    }

}
