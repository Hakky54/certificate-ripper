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
package nl.altindag.crip.client.websocket;

import nl.altindag.ssl.model.ClientConfig;
import nl.altindag.ssl.util.AuthenticatorUtils;
import nl.altindag.ssl.util.ClientRunnable;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class WebSocketClientRunnable implements ClientRunnable {

    @Override
    public void run(ClientConfig clientConfig, URI uri) {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .sslContext(clientConfig.getSslFactory().getSslContext());

        clientConfig.getProxy()
                .map(this::createProxySelector)
                .ifPresent(clientBuilder::proxy);

        clientConfig.getPasswordAuthentication()
                .map(AuthenticatorUtils::create)
                .ifPresent(clientBuilder::authenticator);

        Optional<Duration> timeout = clientConfig.getTimeout();
        timeout.ifPresent(clientBuilder::connectTimeout);

        WebSocketListener listener = new WebSocketListener(new CountDownLatch(1));
        clientBuilder.build()
                .newWebSocketBuilder()
                .buildAsync(uri, listener).join();
        timeout.ifPresentOrElse(listener::waitTillTimeout, listener::waitEndless);
    }

    private ProxySelector createProxySelector(Proxy proxy) {
        return new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                return List.of(proxy);
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

            }
        };
    }

}
