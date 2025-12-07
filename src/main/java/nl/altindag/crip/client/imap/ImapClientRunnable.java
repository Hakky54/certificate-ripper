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
package nl.altindag.crip.client.imap;

import nl.altindag.ssl.model.ClientConfig;
import nl.altindag.ssl.util.ClientRunnable;
import nl.altindag.sude.Logger;
import nl.altindag.sude.LoggerFactory;
import org.apache.commons.net.SocketClient;
import org.apache.commons.net.imap.AuthenticatingIMAPClient;

import java.net.URI;
import java.time.Duration;

public final class ImapClientRunnable implements ClientRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImapClientRunnable.class);

    @Override
    public void run(ClientConfig clientConfig, URI uri) {
        SocketClient client = new AuthenticatingIMAPClient(true, clientConfig.getSslFactory().getSslContext());
        clientConfig.getProxy().ifPresent(client::setProxy);
        clientConfig.getTimeout()
                .map(Duration::toMillis)
                .map(Long::intValue)
                .ifPresent(client::setDefaultTimeout);

        try {
            client.connect(uri.getHost(), uri.getPort());
        } catch (Exception e) {
            LOGGER.debug(String.format("Could not connect to %s:%d", uri.getHost(), uri.getPort()), e);
        }
    }

}
