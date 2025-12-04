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
package nl.altindag.crip.model.export;

import nl.altindag.crip.model.Request;

import java.nio.file.Path;
import java.util.List;

public abstract class ExportRequest extends Request {

    private String destination;

    public ExportRequest(List<String> urls) {
        super(urls);
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setDestination(Path destination) {
        this.destination = destination.toString();
    }

    abstract ExportMode getExportMode();

    @Override
    public String toString() {
        return String.format("export %s --destination=%s %s", getExportMode(), destination, super.toString());
    }
}
