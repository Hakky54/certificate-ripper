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

import nl.altindag.crip.command.VersionProvider;
import picocli.CommandLine.Command;

@Command(name = "pkcs12",
        aliases = {"p12"},
        description = "Export the extracted certificate to a PKCS12/p12 type truststore",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class)
public class Pkcs12ExportCommand extends KeyStoreExportCommand {

    @Override
    String getKeyStoreType() {
        return "PKCS12";
    }

    @Override
    String getFileExtension() {
        return ".p12";
    }

}
