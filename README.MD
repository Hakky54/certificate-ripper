[![Actions Status](https://github.com/Hakky54/certificate-ripper/workflows/Build/badge.svg)](https://github.com/Hakky54/certificate-ripper/actions)

# Certificate Ripper 🔐
A CLI tool to extract server certificates

## Demo
![alt text](https://github.com/Hakky54/certificate-ripper/blob/master/images/demo.gif?raw=true)

## Usage
### Mac OS X - Homebrew 🍺
```bash
brew tap hakky54/cerip
brew install cerip
cerip url=https://stackoverflow.com/
```
### Windows
<sub><sup>TODO</sub></sup>

### Linux
<sub><sup>TODO</sub></sup>

### Using Executable JAR
**Minimum requirements:**
1. Java 8
3. A terminal

**Setup**
1. Clone the project `git clone https://github.com/Hakky54/certificate-ripper.git`
2. Run once `./mvnw clean package` to download the required dependencies
3. Go the to executable jar with `cd target/`
4. Run it with `java -jar cerip.jar url=https://stackoverflow.com/`

### CLI Options
- format=pem

## Contributing

There are plenty of ways to contribute to this project:

* Give it a star
* Share it with a [![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=Easily%20extract%20server%20certificates&url=https://github.com/Hakky54/certificate-ripper&via=hakky541&hashtags=certificate,security,https,ssl,tls,developer,java)
* Submit a PR
