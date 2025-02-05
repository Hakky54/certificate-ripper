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

import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.ProviderException;

public final class CertificateRipperProvider extends Provider {

    private static final class MockAppleProviderService extends Provider.Service {

        public MockAppleProviderService(Provider p, String type, String algo, String cn) {
            super(p, type, algo, cn, null, null);
        }

        @Override
        public Object newInstance(Object constructorParameter) throws NoSuchAlgorithmException {
            String type = getType();
            String algo = getAlgorithm();
            try {
                if (type.equals("KeyStore") && algo.equals("KeychainStore") || algo.equals("KeychainStore-ROOT")) {
                    return new DummyKeychainStore();
                }
            } catch (Exception ex) {
                throw new NoSuchAlgorithmException("Error constructing " + type + " for " + algo, ex);
            }
            throw new ProviderException("No impl for " + algo + " " + type);
        }
    }

    public CertificateRipperProvider() {
        super("CertificateRipper", 1.0, "Certificate Ripper Security Provider");

        final Provider provider = this;
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            putService(new MockAppleProviderService(provider, "KeyStore", "KeychainStore", "apple.security.KeychainStore$USER"));
            putService(new MockAppleProviderService(provider, "KeyStore", "KeychainStore-ROOT", "apple.security.KeychainStore$ROOT"));
            return null;
        });
    }

}