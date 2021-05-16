package nl.altindag.ssl;

import nl.altindag.ssl.command.CertificateRipper;
import picocli.CommandLine;

public class App {

    public static void main(String[] applicationArguments) {
        new CommandLine(new CertificateRipper())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(applicationArguments);
    }

}
