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
package nl.altindag.crip.request.export;

import java.util.List;

public abstract class KeystoreExportRequest extends ExportRequest {

    private String password;

    public KeystoreExportRequest(List<String> urls) {
        super(urls);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        if (password == null) {
            return super.toString();
        }

        return super.toString() + " --password=" + password;
    }
}
