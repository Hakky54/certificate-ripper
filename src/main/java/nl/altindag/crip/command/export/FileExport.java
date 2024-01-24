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
package nl.altindag.crip.command.export;

import nl.altindag.crip.command.SharedProperties;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

class FileExport {

    private static final Path CURRENT_DIRECTORY = Paths.get(System.getProperty("user.dir"));

    @Mixin
    protected SharedProperties sharedProperties;

    @Option(names = {"-d", "--destination"}, description = "Destination of the to be stored file. Default is current directory if none is provided.")
    private String destination;

    protected Optional<Path> getDestination() {
        if (destination == null) {
            return Optional.empty();
        }

        return Optional.of(Paths.get(destination));
    }

    protected String reformatFileName(String fileName) {
        return fileName.replaceAll("[.\\\\]+", "-");
    }

    protected Path getCurrentDirectory() {
        return CURRENT_DIRECTORY;
    }
}
