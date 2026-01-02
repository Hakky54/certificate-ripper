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
package nl.altindag.crip.client.postgres;

import nl.altindag.ssl.model.ClientConfig;
import nl.altindag.ssl.util.ClientRunnable;
import nl.altindag.sude.Logger;
import nl.altindag.sude.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;

/**
 * <a href="https://www.postgresql.org/docs/current/protocol-flow.html#PROTOCOL-FLOW-SSL">Protocol - SSL Flow</a>
 * <a href="https://www.postgresql.org/docs/current/protocol-message-formats.html#PROTOCOL-MESSAGE-FORMATS-SSLREQUEST">Protocol message format - SSL request</a>
 */
public final class PostgresClientRunnable implements ClientRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresClientRunnable.class);

    private static final int SSL_REQUEST_MESSAGE_LENGTH = 8;
    private static final int SSL_REQUEST_CODE = 80877103;
    private static final char SECURE_CODE = 'S';

    @Override
    public void run(ClientConfig clientConfig, URI uri) {
        try (Socket socket = new Socket(uri.getHost(), uri.getPort());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            Optional<Integer> timeout = clientConfig.getTimeout()
                    .map(Duration::toMillis)
                    .map(Long::intValue);

            if (timeout.isPresent()) {
                socket.setSoTimeout(timeout.get());
            }

            out.writeInt(SSL_REQUEST_MESSAGE_LENGTH);
            out.writeInt(SSL_REQUEST_CODE);
            out.flush();

            byte response = in.readByte();
            if (response != SECURE_CODE) {
                LOGGER.debug("The server does not support SSL or refuses to connect.");
                return;
            }

            try (SSLSocket sslSocket = (SSLSocket) clientConfig.getSslFactory().getSslSocketFactory().createSocket(socket, uri.getHost(), uri.getPort(), true)) {
                sslSocket.startHandshake();
            }
        } catch (IOException e) {
            LOGGER.debug(String.format("Could not connect to %s:%d", uri.getHost(), uri.getPort()), e);
        }
    }

}
