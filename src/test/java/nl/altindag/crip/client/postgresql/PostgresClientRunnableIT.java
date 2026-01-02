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
package nl.altindag.crip.client.postgresql;

import nl.altindag.console.ConsoleCaptor;
import nl.altindag.crip.CertificateRipper;
import nl.altindag.crip.request.Request;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresClientRunnableIT {

    private static final Pattern CAPTURED_CERTIFICATES = Pattern.compile("\\* [1-9]+: postgresql://localhost:5432/");

    @Test
    void shouldPrintCertificates() throws IOException {
        ConsoleCaptor consoleCaptor = new ConsoleCaptor();
        Process process = new ProcessBuilder("docker", "run", "--rm",
                "-e" ,"POSTGRES_PASSWORD=password",
                "-p", "5432:5432", "postgres:15",
                "-c", "ssl=on",
                "-c", "ssl_cert_file=/etc/ssl/certs/ssl-cert-snakeoil.pem",
                "-c", "ssl_key_file=/etc/ssl/private/ssl-cert-snakeoil.key"
        ).start();

        Request request = CertificateRipper.print("postgresql://localhost:5432/").build();

        Awaitility.await()
                .atMost(60, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    request.run();
                    String standardOutput = String.join(System.lineSeparator(), consoleCaptor.getStandardOutput());
                    assertThat(CAPTURED_CERTIFICATES.matcher(standardOutput).find()).isTrue();
                });

        process.destroy();
        consoleCaptor.close();
    }

}
