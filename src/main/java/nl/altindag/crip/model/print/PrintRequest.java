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
package nl.altindag.crip.model.print;

import nl.altindag.crip.model.Request;

import java.util.List;

import static nl.altindag.crip.model.print.Format.X509;

public class PrintRequest extends Request {

    private Format format = X509;

    public PrintRequest(List<String> urls) {
        super(urls);
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    @Override
    public String toString() {
        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append("print").append(" ")
                .append("--format=").append(format).append(" ");

        getUrls().forEach(url -> requestBuilder.append("--url=").append(url).append(" "));
        return requestBuilder.append(super.toString()).toString();
    }

}
