package nl.altindag.ssl.command.export;

import nl.altindag.ssl.command.SharedProperties;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.net.MalformedURLException;
import java.net.URL;

public class FileExport {

    @Mixin
    protected SharedProperties sharedProperties;

    @Option(names = {"-d", "--destination"}, description = "Destination of the to be stored file. Default is current directory if none is provided.")
    protected String destination = System.getProperty("user.dir");

}
