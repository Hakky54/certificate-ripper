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
package nl.altindag.crip.client.ftp;

import nl.altindag.console.ConsoleCaptor;
import nl.altindag.crip.CertificateRipper;
import nl.altindag.crip.request.Request;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class FtpClientRunnableIT {

    private static final Pattern CAPTURED_CERTIFICATES = Pattern.compile("\\* [1-9]+: ftps://localhost:2231");

    @Test
    void shouldPrintCertificates() throws IOException {
        URL dockerComposeFile = FtpClientRunnableIT.class.getClassLoader().getResource("docker-compose/ftp/docker-compose.yaml");
        assertThat(dockerComposeFile).isNotNull();

        ConsoleCaptor consoleCaptor = new ConsoleCaptor();
        new ProcessBuilder(
                "docker",
                "compose",
                "--file=" + dockerComposeFile.getPath(),
                "up"
        ).start();

        Request request = CertificateRipper.print("ftps://localhost:2231").build();

        Awaitility.await()
                .atMost(2, TimeUnit.MINUTES)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    request.run();
                    String standardOutput = String.join(System.lineSeparator(), consoleCaptor.getStandardOutput());
                    assertThat(CAPTURED_CERTIFICATES.matcher(standardOutput).find()).isTrue();
                });

        new ProcessBuilder(
                "docker",
                "compose",
                "--file=" + dockerComposeFile.getPath(),
                "down"
        ).start();

        consoleCaptor.close();
    }

}
