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
package nl.altindag.crip.client.smtp;

import nl.altindag.console.ConsoleCaptor;
import nl.altindag.crip.CertificateRipper;
import nl.altindag.crip.request.Request;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class SmtpClientRunnableIT {

    private static final Pattern CAPTURED_CERTIFICATES = Pattern.compile("\\* [1-9]+: smtps://smtp-mail\\.outlook\\.com:587");

    @Test
    void shouldPrintCertificates() {
        ConsoleCaptor consoleCaptor = new ConsoleCaptor();
        Request request = CertificateRipper.print("smtps://smtp-mail.outlook.com:587").build();

        Awaitility.await()
                .atMost(60, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    request.run();
                    String standardOutput = String.join(System.lineSeparator(), consoleCaptor.getStandardOutput());
                    assertThat(CAPTURED_CERTIFICATES.matcher(standardOutput).find()).isTrue();
                });

        consoleCaptor.close();
    }

}
