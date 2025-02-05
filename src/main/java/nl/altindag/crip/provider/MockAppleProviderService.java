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
package nl.altindag.crip.provider;

import java.security.Provider;
import java.security.ProviderException;

final class MockAppleProviderService extends Provider.Service {

    public MockAppleProviderService(Provider p, String type, String algo, String cn) {
        super(p, type, algo, cn, null, null);
    }

    @Override
    public Object newInstance(Object constructorParameter) {
        String type = getType();
        String algo = getAlgorithm();
        if ("KeyStore".equals(type) && "KeychainStore".equals(algo) || "KeychainStore-ROOT".equals(algo)) {
            return new DummyKeychainStore();
        }
        throw new ProviderException("No impl for " + algo + " " + type);
    }

}
