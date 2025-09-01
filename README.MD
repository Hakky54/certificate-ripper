[![Actions Status](https://github.com/Hakky54/certificate-ripper/workflows/Build/badge.svg)](https://github.com/Hakky54/certificate-ripper/actions)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=io.github.hakky54%3Acertificate-ripper&metric=security_rating)](https://sonarcloud.io/dashboard?id=io.github.hakky54%3Acertificate-ripper)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=io.github.hakky54%3Acertificate-ripper&metric=coverage)](https://sonarcloud.io/dashboard?id=io.github.hakky54%3Acertificate-ripper)
[![Apache2 license](https://img.shields.io/badge/license-Aache2.0-blue.svg)](https://github.com/Hakky54/sslcontext-kickstart/blob/master/LICENSE)
[![GitHub stars chart](https://img.shields.io/badge/github%20stars-chart-blue.svg)](https://seladb.github.io/StarTrack-js/#/preload?r=hakky54,certificate-ripper)

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/dashboard?id=io.github.hakky54%3Acertificate-ripper)


# Certificate Ripper 🔐
A CLI tool to extract server certificates

# Introduction
Certificate ripper came to life when I was curious to learn about writing OS native apps. It started as a pet project and I wanted to create a native app by writing it in Java.
During my work I discovered that extracting certificates in other tools can sometimes be troublesome, so I used that as a use-case to create an app in Java, compile that to native OS app so others don't need Java to run it.
It made my work easier for maintaining trust-stores and I hope it made others life also easier.

I have created this tool with ❤️ and passion, mostly during evening and night hours. If you use my tool and want to appreciate the work I have done, please consider to sponsor this project as a way to contribute back to the community.
There are 3 options available to pick from: [GitHub](https://github.com/sponsors/Hakky54), [Ko-fi](https://ko-fi.com/hakky54) and [Open Collective](https://opencollective.com/hakky54)

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

## Installing

- Mac OS X - Homebrew 🍺
  - Run `brew tap hakky54/crip && brew install crip`
- Linux & Windows
  - Download the latest binary here: [Releases](https://github.com/Hakky54/certificate-ripper/releases)

#### Contributed/Unofficial Installation Methods
- Arch-Linux (AUR)
  - Install the [certificate-ripper-bin](https://aur.archlinux.org/packages/certificate-ripper-bin) AUR package
- NixOS (nixpkgs)
  - Run `nix-shell -p certificate-ripper` or add `pkgs.certificate-ripper` to your `configuration.nix` file
- Windows
  - Chocolatey 🍫 
    - Run `choco install crip`
  - Scoop 🍨
    - Run `scoop install extras/crip`

## Build locally
<details>
  <summary>Build native executable</summary>

  **Minimum requirements:**
  1. GraalVM 17 with Native Image
  2. Maven
  3. Terminal
  
  **Additional OS specific requirements**
  - Linux: `sudo apt-get update && sudo apt-get install build-essential libz-dev zlib1g-dev -y`
  - Mac: `xcode-select --install`
  - Windows: Visual Studio app
  
  ```text
  mvn clean install -Pnative-image \
   && ./target/crip print --url=https://youtube.com/
  ```
  
  The os native executable binary will be available under the target directory having the file name `crip`

</details>

<details>
  <summary>Build java fat jar</summary>

  **Minimum requirements:**
  1. Java 11
  2. Maven
  3. Terminal

  ```text
  mvn clean install \
   && java -jar target/crip.jar print --url=https://youtube.com/
  ```

  The fat jar will be available under the target directory having the file name `crip.jar`

</details>

## CLI Options
```text
Usage: crip [COMMAND]
Commands:
  print             Prints the extracted certificates to the console
  export p12        Export the extracted certificate to a PKCS12/p12 type truststore
  export jks        Export the extracted certificate to a JKS (Java KeyStore) type truststore
  export der        Export the extracted certificate to a binary form also known as DER
  export pem        Export the extracted certificate to a base64 encoded string also known as PEM
  
Usage: crip print
Prints the extracted certificates to the console
  -f, --format              To be printed certificate format. This option is not required. Default is human-readable.
  -u, --url                 Url of the target server to extract the certificates. Can be provided multiple times.

Usage: crip export pkcs12
Export the extracted certificate to a PKCS12/p12 type truststore
  -u, --url                 Url of the target server to extract the certificates. Can be provided multiple times.
  -p, --password            TrustStore password. This option is not required. Default is changeit.
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

Other additional options applicable for all commands
  -t, --timeout             Amount of milliseconds till the ripping should timeout
      --resolve-ca          Indicator to automatically resolve the root ca. Possible options: true, false
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
crip export pem \
-u=https://stackoverflow.com \
--proxy-host=my-host.com \
--proxy-port=1234 \
--proxy-user=foo \
--proxy-password
```

### Combining certificates
```bash
crip export pem -u=https://github.com --combined=true
```

### Defining custom file name
Works only with the combined option while only specifying a single url.
```bash
crip export pem -u=https://github.com --combined=true --destination=/path/to/export/github-chain.crt
```

### Extract system certificates
```bash
crip export pem -u=system
```

### Extract from a websocket server
```bash
crip export pem -u=wss://echo.websocket.org
```

## Contributing

There are plenty of ways to contribute to this project:

* Give it a star
* Make a donation through [GitHub](https://github.com/sponsors/Hakky54) or [open collective](https://opencollective.com/hakky54)
* Share it with a [![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=Easily%20extract%20server%20certificates&url=https://github.com/Hakky54/certificate-ripper&via=hakky541&hashtags=certificate,security,https,ssl,tls,developer,java)
* Submit a PR
