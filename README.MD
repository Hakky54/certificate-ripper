[![Actions Status](https://github.com/Hakky54/certificate-ripper/workflows/Build/badge.svg)](https://github.com/Hakky54/certificate-ripper/actions)

# Certificate Ripper 🔐
A CLI tool to extract server certificates

## Demo
![alt text](https://github.com/Hakky54/certificate-ripper/blob/master/images/demo.gif?raw=true)

## Advantages
- It is fast
- Easy to use
- No openssl required
- Runs on any Operating System
- Can be used with or without Java, native executables are present in the releases
- Extracts all the sub-fields of the certificate
- Certificates can be formatted to PEM format
- Bulk extraction of multiple different urls with a single command is possible
- Extracted certificates can be stored automatically into a p12 truststore

## Installing & Usage
### Mac OS X - Homebrew 🍺
```bash
brew tap hakky54/crip
brew install crip
crip print --url=https://stackoverflow.com/
```
### Windows
1. Download the latest binary here: [Releases](https://github.com/Hakky54/certificate-ripper/releases)
2. Extract the compressed file
3. Start cmd and `cd` to the extracted file
4. Run `start /b "" "crip.exe" print --url=https://stackoverflow.com/`

### Linux

#### Arch-Linux (AUR)
1. Install the [certificate-ripper-bin](https://aur.archlinux.org/packages/certificate-ripper-bin) AUR package
2. Run `crip print --url=https://stackoverflow.com/`

#### From Source
1. Download the latest binary here: [Releases](https://github.com/Hakky54/certificate-ripper/releases)
2. Extract the compressed file
3. Add the reference to your environment variables: `export CRIP_HOME=/path/to/crip/binary`
4. Run `crip print --url=https://stackoverflow.com/`

### Using Executable JAR
**Minimum requirements:**
1. Java 8
3. A terminal

**Setup**
1. Download the latest JAR here: [Releases](https://github.com/Hakky54/certificate-ripper/releases)
2. Run it with `java -jar crip.jar print --url=https://youtube.com/`

### CLI Options
```text
Usage: crip [COMMAND]
Commands:
  print   Prints the extracted certificates to the console
  export  Export the extracted certificate to a PKCS12/p12 type truststore
  
Usage: crip print [-f=<format>] -u=<urls> [-u=<urls>]...
Prints the extracted certificates to the console
  -f, --format              To be printed certificate format. This option is not required. Default is human-readable.
  -u, --url                 Url of the target server to extract the certificates. Can be provided multiple times.
  
Usage: crip export [-p=<password>] -u=<urls> [-u=<urls>]...
Export the extracted certificate to a PKCS12/p12 type truststore
  -p, --password            TrustStore password. This option is not required. Default is changeit.
  -u, --url                 Url of the target server to extract the certificates. Can be provided multiple times.
  -d, --destination         Destination of the to be stored truststore file. Default is current directory if none is provided.
```

## Example usages
### Single export
```bash
crip export -u=https://github.com
```

### Bulk export
```bash
crip export \
-u=https://youtube.com \
-u=https://github.com \
-u=https://stackoverflow.com \
-u=https://facebook.com
```

### Specify custom truststore destination path
```bash
crip export -u=https://github.com -d=/path/to/directory
```

### Print in human-readable format 
```bash
crip print -u=https://github.com
```

### Print in PEM format
```bash
crip print -u=https://github.com -f=pem
```

### Batch print in PEM format
```bash
crip print -f=pem \
-u=https://youtube.com \
-u=https://github.com \
-u=https://stackoverflow.com \
-u=https://facebook.com
```

## Contributing

There are plenty of ways to contribute to this project:

* Give it a star
* Share it with a [![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=Easily%20extract%20server%20certificates&url=https://github.com/Hakky54/certificate-ripper&via=hakky541&hashtags=certificate,security,https,ssl,tls,developer,java)
* Submit a PR
