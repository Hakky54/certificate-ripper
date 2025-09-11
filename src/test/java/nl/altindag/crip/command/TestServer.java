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
package nl.altindag.crip.command;

import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.server.service.Server;
import nl.altindag.ssl.util.SSLSessionUtils;

public final class TestServer {

    private static TestServer INSTANCE;

    private final Server serverOne;
    private final Server serverTwo;
    private final SSLFactory sslFactoryForServerOne;
    private final SSLFactory sslFactoryForServerTwo;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            getInstance().serverOne.stop();
            getInstance().serverTwo.stop();
        }));
    }

    private TestServer() {
        char[] keyStorePassword = "secret".toCharArray();
        sslFactoryForServerOne = SSLFactory.builder()
                .withIdentityMaterial("keystore/server/server-one/identity.jks", keyStorePassword)
                .withTrustMaterial("keystore/server/server-one/truststore.jks", keyStorePassword)
                .build();

        sslFactoryForServerTwo = SSLFactory.builder()
                .withIdentityMaterial("keystore/server/server-two/identity.jks", keyStorePassword)
                .withTrustMaterial("keystore/server/server-two/truststore.jks", keyStorePassword)
                .build();

        serverOne = Server.builder(sslFactoryForServerOne)
                .withPort(8443)
                .build();

        serverTwo = Server.builder(sslFactoryForServerTwo)
                .withPort(8444)
                .build();
    }

    public SSLFactory getSslFactoryForServerOne() {
        return sslFactoryForServerOne;
    }

    public SSLFactory getSslFactoryForServerTwo() {
        return sslFactoryForServerTwo;
    }

    public static void invalidateServerCaches() {
        SSLSessionUtils.invalidateServerCaches(getInstance().sslFactoryForServerOne);
        SSLSessionUtils.invalidateServerCaches(getInstance().sslFactoryForServerTwo);
    }

    public static TestServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TestServer();
        }
        return INSTANCE;
    }

}
