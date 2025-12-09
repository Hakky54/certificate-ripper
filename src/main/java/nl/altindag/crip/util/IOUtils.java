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
package nl.altindag.crip.util;

import nl.altindag.ssl.exception.GenericIOException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * <strong>NOTE:</strong>
 * Please don't use this class directly as it is part of the internal API. Class name and methods can be changed any time.
 *
 * @author Hakan Altindag
 */
public final class IOUtils {

    private static final Path CURRENT_DIRECTORY = Paths.get(System.getProperty("user.dir"));

    private IOUtils() {}

    public static void write(Path path, byte[] data) {
        try {
            createDirectoriesIfAbsent(path);
            Files.write(path, data, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new GenericIOException(e);
        }
    }

    private static void createDirectoriesIfAbsent(Path absoluteFilePath) throws IOException {
        Path parentDirectories = absoluteFilePath.getParent();
        if (Files.notExists(parentDirectories)) {
            Files.createDirectories(parentDirectories);
        }
    }

    public static Path resolveDestination(Path path) {
        if (Files.isDirectory(path)) {
            return path;
        }

        if (!path.isAbsolute()) {
            return resolveDestination(path.toAbsolutePath().normalize());
        }

        if (path.getParent() != null) {
            return path;
        }

        return getCurrentDirectory();
    }

    public static Path resolveDestination(Path path, String fileName) {
        if (Files.isDirectory(path)) {
            return path.resolve(fileName);
        }

        if (!path.isAbsolute()) {
            return resolveDestination(path.toAbsolutePath().normalize(), fileName);
        }

        return path;
    }

    public static Path getCurrentDirectory() {
        return CURRENT_DIRECTORY;
    }

}
