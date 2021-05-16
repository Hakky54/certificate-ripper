package nl.altindag.ssl.command;

import picocli.CommandLine.Command;

@Command(
        name = "crip",
        version = "crip v0.0.2",
        description = "CLI tool to extracts server certificates",
        subcommands = {
                PrintCommand.class,
                ExportCommand.class
        }
)
public class CertificateRipper {}
