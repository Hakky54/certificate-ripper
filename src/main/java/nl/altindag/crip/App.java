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

import com.sun.jna.platform.win32.Kernel32;
import nl.altindag.crip.command.CertificateRipper;
import nl.altindag.crip.provider.CertificateRipperProvider;
import nl.altindag.crip.util.HelpFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] applicationArguments) {
        applyWorkaroundForNativeExecutable();

        new CommandLine(new CertificateRipper())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setHelpFactory(new HelpFactory())
                .execute(applicationArguments);
    }

    private static void applyWorkaroundForNativeExecutable() {
        // Temporally ignoring KeychainStore as it does not work with Graal VM yet.
        // The actual call to get the KeychainStore from the Apple Provider will be intercepted, and it will return a dummy keystore
        // See here for the related issue https://github.com/oracle/graal/issues/10387
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            Security.insertProviderAt(new CertificateRipperProvider(), 1);
        }

        // Temporally enforcing chcp 65001 to support UTF-8 on windows. This code snippet can be removed in the future
        // when the GraalVM issue https://github.com/oracle/graal/issues/11214 is resolved
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            int cp = 65001;
            if (Kernel32.INSTANCE.SetConsoleCP(cp)) {
                LOGGER.debug("Successfully set the console CP to [{}]", cp);
            }

            if (Kernel32.INSTANCE.SetConsoleOutputCP(cp)) {
                LOGGER.debug("Successfully set the console output CP to [{}]", cp);
            }
        }

        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
    }

}
