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

import nl.altindag.crip.model.ExportMode;

import java.util.List;

import static nl.altindag.crip.model.ExportMode.PEM;


public class PemExportRequest extends CombineableExportRequest {

    private Boolean includeHeader = true;

    public PemExportRequest(List<String> urls) {
        super(urls);
    }

    public void setIncludeHeader(boolean includeHeader) {
        this.includeHeader = includeHeader;
    }

    @Override
    ExportMode getExportMode() {
        return PEM;
    }

    @Override
    public String toString() {
        return String.format("%s --include-header=%s", super.toString(), includeHeader);
    }
}
