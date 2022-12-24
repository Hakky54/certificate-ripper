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

import nl.altindag.crip.command.export.ExportCommand;
import picocli.CommandLine.Command;

@Command(
        name = "crip",
        version = "crip v2.0.0",
        description = "CLI tool to extracts server certificates",
        subcommands = {
                PrintCommand.class,
                ExportCommand.class
        }
)
public class CertificateRipper {}
