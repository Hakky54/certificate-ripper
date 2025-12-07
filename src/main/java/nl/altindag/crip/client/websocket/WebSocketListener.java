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

import nl.altindag.sude.Logger;
import nl.altindag.sude.LoggerFactory;

import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class WebSocketListener implements WebSocket.Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketListener.class);

    private final CountDownLatch latch;

    public WebSocketListener(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        LOGGER.debug("WebSocket opened");
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        LOGGER.debug(String.format("WebSocket text received: %s", data));
        latch.countDown();
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        LOGGER.debug("WebSocket error", error);
        WebSocket.Listener.super.onError(webSocket, error);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void waitTillTimeout(Duration timeout) {
        try {
            latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // skip
        }
    }

    public void waitEndless() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            // skip
        }
    }

}
