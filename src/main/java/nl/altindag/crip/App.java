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

import nl.altindag.crip.command.CertificateRipper;
import nl.altindag.crip.util.Constants;
import nl.altindag.crip.util.HelpFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(versionProvider = App.VersionProvider.class)
public class App {

    public static void main(String[] applicationArguments) {
        new CommandLine(new CertificateRipper())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setHelpFactory(new HelpFactory())
                .execute(applicationArguments);
    }

    public static class VersionProvider implements CommandLine.IVersionProvider {
        public String[] getVersion() {
            return new String[]{String.format("%s %s", Constants.APP_NAME, Constants.APP_VERSION)};
        }
    }
}
