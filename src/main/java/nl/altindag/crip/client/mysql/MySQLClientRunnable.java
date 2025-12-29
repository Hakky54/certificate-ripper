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
package nl.altindag.crip.client.mysql;

import nl.altindag.ssl.model.ClientConfig;
import nl.altindag.ssl.util.ClientRunnable;
import nl.altindag.sude.Logger;
import nl.altindag.sude.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;

/**
 * <a href="https://github.com/openssl/openssl/blob/master/apps/s_client.c">Source</a>
 * Rewritten MySQL section with Claude Sonnet 4.5
 */
public final class MySQLClientRunnable implements ClientRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MySQLClientRunnable.class);

    private static final int SSL_FLAG = 0x800;
    private static final byte[] SSL_REQUEST = {
            // payload_length, sequence_id
            (byte) 0x20, 0x00, 0x00, 0x01,
            // payload
            // capability flags, CLIENT_SSL always set
            (byte) 0x85, (byte) 0xae, 0x7f, 0x00,
            // max-packet size
            0x00, 0x00, 0x00, 0x01,
            // character set
            0x21,
            // string[23] reserved (all [0])
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    @Override
    public void run(ClientConfig clientConfig, URI uri) {
        try {
            Socket socket = new Socket(uri.getHost(), uri.getPort());
            Optional<Integer> timeout = clientConfig.getTimeout()
                    .map(Duration::toMillis)
                    .map(Long::intValue);

            if (timeout.isPresent()) {
                socket.setSoTimeout(timeout.get());
            }

            byte[] buffer = new byte[8192];

            // Receiving Initial Handshake packet
            int bytesRead = socket.getInputStream().read(buffer);

            int capabilityFlags = getCapabilityFlags(bytesRead, buffer);
            if ((capabilityFlags & SSL_FLAG) == 0) {
                throw new IOException("MySQL server does not support SSL");
            }

            // Sending SSL Handshake packet
            socket.getOutputStream().write(SSL_REQUEST);
            socket.getOutputStream().flush();

            SSLSocket sslSocket = (SSLSocket) clientConfig.getSslFactory().getSslSocketFactory().createSocket(socket, uri.getHost(), uri.getPort(), true);
            sslSocket.setUseClientMode(true);
            sslSocket.startHandshake();
        } catch (Exception e) {
            LOGGER.debug(String.format("Could not connect to %s:%d", uri.getHost(), uri.getPort()), e);
        }
    }

    private static int getCapabilityFlags(int bytesRead, byte[] buffer) throws IOException {
        if (bytesRead < 21) {
            throw new IOException("MySQL packet too short");
        }

        int packetLength = (buffer[0] & 0xFF) + ((buffer[1] & 0xFF) << 8) + ((buffer[2] & 0xFF) << 16);
        if (bytesRead != 4 + packetLength) {
            throw new IOException("MySQL packet length does not match");
        }

        // protocol version[1]
        if (buffer[4] != 0x0A) {
            throw new IOException("Only MySQL protocol version 10 is supported");
        }

        int pos = 5;
        // server version[string+NULL]
        while (pos < bytesRead && buffer[pos] != 0) {
            pos++;
        }

        pos++; // skip NULL terminator
        if (pos + 15 > bytesRead) {
            throw new IOException("MySQL server handshake packet is broken");
        }

        pos += 12; // skip over conn id[4] + SALT[8]
        if (buffer[pos++] != 0) {
            throw new IOException("MySQL packet is broken");
        }

        // capability flags[2]
        return (buffer[pos] & 0xFF) + ((buffer[pos + 1] & 0xFF) << 8);
    }

}
