package nl.altindag.crip.command.export;

import picocli.CommandLine.Option;

public class CombinableFileExport extends FileExport {

    @Option(names = {"-c", "--combined"}, description = "Indicator to either combine all of the certificate into one file for a given url or export into individual files.")
    protected Boolean combined = false;

}
