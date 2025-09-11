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
package nl.altindag.crip.command;

import nl.altindag.console.ConsoleCaptor;
import nl.altindag.log.LogCaptor;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.server.service.Server;
import nl.altindag.ssl.util.SSLSessionUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import picocli.CommandLine;

import java.util.Collections;
import java.util.List;

public class BaseTest {

    protected static CommandLine cmd;
    protected static ConsoleCaptor consoleCaptor;
    private static List<LogCaptor> mutedLogs;

    @BeforeAll
    static void setupCertificateRipper() {
        TestServer.getInstance();
        mutedLogs = Collections.singletonList(LogCaptor.forName("io.netty"));
        mutedLogs.forEach(LogCaptor::disableConsoleOutput);
    }

    @AfterAll
    static void stopServerAndCloseConsoleCaptor() {
        consoleCaptor.close();
        mutedLogs.forEach(LogCaptor::close);
    }

    @BeforeEach
    void setupServerAndConsoleCaptor() {
        CertificateRipper certificateRipper = new CertificateRipper();
        cmd = new CommandLine(certificateRipper)
                .setCaseInsensitiveEnumValuesAllowed(true);

        if (consoleCaptor == null) {
            consoleCaptor = ConsoleCaptor.builder()
                    .allowEmptyLines(true)
                    .allowTrimmingWhiteSpace(false)
                    .build();
        }

        TestServer.invalidateServerCaches();

        consoleCaptor.clearOutput();
    }

}

