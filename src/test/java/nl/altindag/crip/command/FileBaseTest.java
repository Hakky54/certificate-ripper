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

import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileBaseTest extends BaseTest {

    protected static final Path TEMP_DIRECTORY = Paths.get(System.getProperty("user.home"), "certificate-ripper-temp");

    @BeforeEach
    void createTempDirAndClearConsoleCaptor() throws IOException {
        if (Files.exists(TEMP_DIRECTORY)) {
            List<Path> files = Files.walk(TEMP_DIRECTORY, 1)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            for (Path file : files) {
                Files.delete(file);
            }

            Files.deleteIfExists(TEMP_DIRECTORY);
        }

        Files.createDirectories(TEMP_DIRECTORY);
        consoleCaptor.clearOutput();
    }

}
