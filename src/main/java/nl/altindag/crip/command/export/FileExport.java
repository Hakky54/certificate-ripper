package nl.altindag.crip.command.export;

import nl.altindag.crip.command.SharedProperties;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

public class FileExport {

    @Mixin
    protected SharedProperties sharedProperties;

    @Option(names = {"-d", "--destination"}, description = "Destination of the to be stored file. Default is current directory if none is provided.")
    protected String destination = System.getProperty("user.dir");

}
