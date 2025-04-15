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
import nl.altindag.ssl.util.ClientRunnable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class WebSocketClientRunnable implements ClientRunnable {

    @Override
    public void run(ClientConfig clientConfig, URI uri) {
        Optional<Duration> timeout = clientConfig.getTimeout();

        WebSocket.Builder webSocketBuilder = HttpClient.newBuilder()
                .sslContext(clientConfig.getSslFactory().getSslContext())
                .build()
                .newWebSocketBuilder();

        timeout.ifPresent(webSocketBuilder::connectTimeout);

        WebSocketListener listener = new WebSocketListener(new CountDownLatch(1));
        webSocketBuilder.buildAsync(uri, listener).join();
        timeout.ifPresentOrElse(listener::waitForResponse, listener::waitForResponse);
    }
}
