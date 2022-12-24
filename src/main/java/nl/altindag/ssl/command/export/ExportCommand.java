package nl.altindag.ssl.command.export;

import picocli.CommandLine.Command;

@Command(name = "export",
        description = "Export the extracted certificate to a provided output type",
        subcommands = {
                Pkcs12ExportCommand.class,
                DerExportCommand.class,
                PemExportCommand.class
        })
public class ExportCommand {

}
