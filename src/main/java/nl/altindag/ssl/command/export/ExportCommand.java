package nl.altindag.ssl.command.export;

import picocli.CommandLine.Command;

@Command(name = "export", subcommands = {
        Pkcs12ExportCommand.class,
        DerExportCommand.class,
        PemExportCommand.class
})
public class ExportCommand {

}
