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
package nl.altindag.crip.model;

public final class ProxyOptions {

    private final String host;
    private final Integer port;
    private final String username;
    private final String password;

    public ProxyOptions(String host, Integer port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        StringBuilder proxyRequestBuilder = new StringBuilder();
        if (host != null) {
            proxyRequestBuilder.append("--proxy-host=").append(host).append(" ");
        }
        if (port != null) {
            proxyRequestBuilder.append("--proxy-port=").append(port).append(" ");
        }
        if (username != null) {
            proxyRequestBuilder.append("--proxy-user=").append(username).append(" ");
        }
        if (password != null) {
            proxyRequestBuilder.append("--proxy-password=").append(password);
        }
        return proxyRequestBuilder.toString();
    }
}
