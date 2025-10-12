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

import nl.altindag.ssl.model.ClientConfig;
import nl.altindag.ssl.util.ClientRunnable;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

public class FtpsClientRunnable implements ClientRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpsClientRunnable.class);

    @Override
    public void run(ClientConfig clientConfig, URI uri) {
        FTPSClient client = new FTPSClient(clientConfig.getSslFactory().getSslContext());
        clientConfig.getProxy().ifPresent(client::setProxy);
        clientConfig.getTimeout()
                .map(Duration::toMillis)
                .map(Math::toIntExact)
                .ifPresent(timeout -> {
                    client.setDefaultTimeout(timeout);
                    client.setConnectTimeout(timeout);
                });

        try {
            client.connect(uri.getHost(), uri.getPort());
            int replyCode = client.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                LOGGER.error("FTP server {} refused connection, reply code: {}", uri.getHost(), replyCode);
            }
        } catch (IOException e) {
            LOGGER.debug("Could not connect to {}:{}", uri.getHost(), uri.getPort(), e);
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                LOGGER.debug("Could not disconnect from {}:{}", uri.getHost(), uri.getPort(), e);
            }
        }
    }

}
