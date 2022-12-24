package nl.altindag.crip;

import nl.altindag.crip.command.CertificateRipper;
import picocli.CommandLine;

public class App {

    public static void main(String[] applicationArguments) {
        new CommandLine(new CertificateRipper())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(applicationArguments);
    }

}
