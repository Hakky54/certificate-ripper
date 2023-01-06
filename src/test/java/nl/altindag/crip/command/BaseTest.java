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
import nl.altindag.crip.Server;
import nl.altindag.crip.ServerUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import picocli.CommandLine;

import java.io.IOException;

public class BaseTest {

    protected static CommandLine cmd;
    protected static ConsoleCaptor consoleCaptor;
    protected static Server serverOne;
    protected static Server serverTwo;

    @BeforeAll
    static void setupCertificateRipperAndServerAndConsoleCaptor() throws IOException {
        CertificateRipper certificateRipper = new CertificateRipper();
        cmd = new CommandLine(certificateRipper)
                .setCaseInsensitiveEnumValuesAllowed(true);
        consoleCaptor = ConsoleCaptor.builder()
                .allowEmptyLines(true)
                .allowTrimmingWhiteSpace(false)
                .build();

        serverOne = ServerUtils.createServerOne();
        serverOne.start();

        serverTwo = ServerUtils.createServerTwo();
        serverTwo.start();
    }

    @AfterAll
    static void stopServerAndCloseConsoleCaptor() {
        serverOne.stop();
        serverTwo.stop();
        consoleCaptor.close();
    }

    @BeforeEach
    void clearConsoleCaptor() {
        consoleCaptor.clearOutput();
    }

}

