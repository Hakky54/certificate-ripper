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
package nl.altindag.crip.command.export;

import nl.altindag.crip.util.IOUtils;
import nl.altindag.ssl.util.KeyStoreUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Collection;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Command(name = "pkcs12", description = "Export the extracted certificate to a PKCS12/p12 type truststore")
public class Pkcs12ExportCommand extends FileExport implements Runnable {

    @Option(names = {"-p", "--password"}, description = "TrustStore password. Default is changeit if none is provided.")
    private String password = "changeit";

    @Override
    public void run() {
        KeyStore trustStore = sharedProperties.getUrlsToCertificates().values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(collectingAndThen(toList(), KeyStoreUtils::createTrustStore));

        Path trustStorePath = getDestination().orElseGet(() -> IOUtils.getCurrentDirectory().resolve("truststore.p12"));

        KeyStoreUtils.write(trustStore, trustStorePath, password.toCharArray());
        System.out.println("Exported certificates to " + trustStorePath.toAbsolutePath());
    }

}
