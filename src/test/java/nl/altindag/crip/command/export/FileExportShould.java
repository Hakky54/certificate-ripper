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

import nl.altindag.crip.command.FileBaseTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FileExportShould extends FileBaseTest {

    @Test
    void getDestinationReturnEmptyIfNotInitialized() {
        FileExport fileExport = new FileExport();
        Optional<Path> destination = fileExport.getDestination();
        assertThat(destination).isEmpty();
    }

    @Test
    void getCurrentDirectory() {
        FileExport fileExport = new FileExport();
        Path currentDirectory = fileExport.getCurrentDirectory();
        assertThat(currentDirectory).isNotNull();
        assertThat(Files.exists(currentDirectory)).isTrue();
    }

}
