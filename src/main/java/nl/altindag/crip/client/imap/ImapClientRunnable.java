package nl.altindag.crip.client.imap;

import nl.altindag.ssl.model.ClientConfig;
import nl.altindag.ssl.util.ClientRunnable;
import org.apache.commons.net.imap.AuthenticatingIMAPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;

public final class ImapClientRunnable implements ClientRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImapClientRunnable.class);

    @Override
    public void run(ClientConfig clientConfig, URI uri) {
        AuthenticatingIMAPClient client = new AuthenticatingIMAPClient(true, clientConfig.getSslFactory().getSslContext());
        clientConfig.getProxy().ifPresent(client::setProxy);
        clientConfig.getTimeout()
                .map(Duration::toMillis)
                .map(Long::intValue)
                .ifPresent(timeout -> {
                    client.setDefaultTimeout(timeout);
                    client.setConnectTimeout(timeout);
                });

        try {
            client.connect(uri.getHost(), uri.getPort());
            client.close();
        } catch (Exception e) {
            LOGGER.debug("Could not connect or close the imap client", e);
        }

    }
}
