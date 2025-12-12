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
import nl.altindag.ssl.util.ProviderUtils;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

public class PostgresClientRunnable implements ClientRunnable {

    @Override
    @SuppressWarnings("EmptyTryBlock")
    public void run(ClientConfig clientConfig, URI uri) {
        try {
            ProviderUtils.configure(clientConfig.getSslFactory());
            System.out.println(Arrays.toString(Security.getProviders()));
            System.out.println(clientConfig.getSslFactory().getSslSocketFactory().getClass().getName());
            System.out.println(SSLContext.getInstance("TLS").getSocketFactory().getClass().getName());
            System.out.println(SSLContext.getDefault().getSocketFactory().getClass().getName());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        String url = String.format("jdbc:%s", uri.toString());

        try (Connection conn = DriverManager.getConnection(url)) {
            // calling getConnection to trigger the SSL handshake
        } catch (SQLException ignored) {

        } finally {
            ProviderUtils.remove();
        }
    }

}
