[![Actions Status](https://github.com/Hakky54/certificate-ripper/workflows/Build/badge.svg)](https://github.com/Hakky54/certificate-ripper/actions)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.hakky54%3Acertificate-ripper&metric=security_rating)](https://sonarcloud.io/dashboard?id=io.github.hakky54%3Acertificate-ripper)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=io.github.hakky54%3Acertificate-ripper&metric=coverage)](https://sonarcloud.io/dashboard?id=io.github.hakky54%3Acertificate-ripper)
[![Apache2 license](https://img.shields.io/badge/license-Aache2.0-blue.svg)](https://github.com/Hakky54/sslcontext-kickstart/blob/master/LICENSE)
[![GitHub stars chart](https://img.shields.io/badge/github%20stars-chart-blue.svg)](https://seladb.github.io/StarTrack-js/#/preload?r=hakky54,certificate-ripper)

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/dashboard?id=io.github.hakky54%3Acertificate-ripper)


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
- Works also behind a proxy

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

#### From Source
1. Download the latest binary here: [Releases](https://github.com/Hakky54/certificate-ripper/releases)
2. Extract the compressed file
3. Add the reference to your environment variables: `export CRIP_HOME=/path/to/crip/binary`
4. Run `crip print --url=https://stackoverflow.com/`

#### Contributed/Unofficial Installation Methods

##### Arch-Linux (AUR)
1. Install the [certificate-ripper-bin](https://aur.archlinux.org/packages/certificate-ripper-bin) AUR package
2. Run `crip print --url=https://stackoverflow.com/`

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
  print             Prints the extracted certificates to the console
  export pkcs12     Export the extracted certificate to a PKCS12/p12 type truststore
  export der        Export the extracted certificate to a binary form also known as DER
  export pem        Export the extracted certificate to a base64 encoded string also known as PEM
  
Usage: crip print
Prints the extracted certificates to the console
  -f, --format              To be printed certificate format. This option is not required. Default is human-readable.
  -u, --url                 Url of the target server to extract the certificates. Can be provided multiple times.

Usage: crip export pkcs12
Export the extracted certificate to a PKCS12/p12 type truststore
  -p, --password            TrustStore password. This option is not required. Default is changeit.
  -u, --url                 Url of the target server to extract the certificates. Can be provided multiple times.
  -d, --destination         Destination of the to be stored file. Default is current directory if none is provided.
      
Usage: crip export der
Export the extracted certificate to a binary form also known as DER
  -u, --url                 Url of the target server to extract the certificates. Can be provided multiple times.
  -c, --combined            Indicator to either combine all of the certificate into one file for a given url or export into individual files.
  -d, --destination         Destination of the to be stored file. Default is current directory if none is provided.

Usage: crip export pem
Export the extracted certificate to a base64 encoded string also known as PEM
  -u, --url                 Url of the target server to extract the certificates. Can be provided multiple times.
  -c, --combined            Indicator to either combine all of the certificate into one file for a given url or export into individual files.
  -d, --destination         Destination of the to be stored file. Default is current directory if none is provided.
      --include-header      Indicator to either omit or include additional information above the BEGIN statement.
      
Proxy options applicable for all commands
      --proxy-host          Proxy host
      --proxy-port          Proxy port
      --proxy-password      Password for authenticating the user for the given proxy
      --proxy-user          User for authenticating the user for the given proxy
```

## Example usages
### Single export
```bash
crip export pkcs12 -u=https://github.com
```

### Bulk export
```bash
crip export pkcs12 \
-u=https://youtube.com \
-u=https://github.com \
-u=https://stackoverflow.com \
-u=https://facebook.com
```

### Specify custom truststore destination path
```bash
crip export pkcs12 -u=https://github.com -d=/path/to/directory
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

### Extracting behind a proxy
```bash
crip print -u=https://stackoverflow.com --proxy-host=my-host.com --proxy-port=1234 --proxy-user=foo --proxy-password
```

### Combining certificates
```bash
crip export pem -u=https://github.com --combined=true
```

### Defining custom file name
Works only with the combined option while only specifying a single url.
```bash
crip export pem -u=https://github.com --combined=true --destionation=/path/to/export/github-chain.crt
```

## Contributing

There are plenty of ways to contribute to this project:

* Give it a star
* Share it with a [![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=Easily%20extract%20server%20certificates&url=https://github.com/Hakky54/certificate-ripper&via=hakky541&hashtags=certificate,security,https,ssl,tls,developer,java)
* Submit a PR
