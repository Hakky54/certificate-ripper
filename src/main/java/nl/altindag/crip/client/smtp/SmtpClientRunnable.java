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

import nl.altindag.ssl.model.ClientConfig;
import nl.altindag.ssl.util.ClientRunnable;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.internal.MailerRegularBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;

public class SmtpClientRunnable implements ClientRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpClientRunnable.class);

    @Override
    public void run(ClientConfig clientConfig, URI uri) {
        MailerRegularBuilderImpl mailerBuilder = MailerBuilder
                .withSMTPServer(uri.getHost(), uri.getPort())
                .withCustomSSLFactoryInstance(clientConfig.getSslFactory().getSslSocketFactory());

        clientConfig.getProxy().ifPresent(proxy -> {
            mailerBuilder.withProxyHost(proxy.address().toString());
            mailerBuilder.withProxyPort(((java.net.InetSocketAddress) proxy.address()).getPort());
        });

        clientConfig.getPasswordAuthentication().ifPresent(authentication -> {
            mailerBuilder.withProxyUsername(authentication.getUserName());
            mailerBuilder.withProxyPassword(new String(authentication.getPassword()));
        });

        clientConfig.getTimeout()
                .map(Duration::toMillis)
                .map(Long::intValue)
                .ifPresent(timeout -> mailerBuilder.withSessionTimeout(timeout));

        Mailer mailer = mailerBuilder
                .buildMailer();

        mailer.testConnection();

        try {
            mailer.close();
        } catch (Exception e) {
            LOGGER.debug("Could not close the mailer client", e);
        }
    }

}
