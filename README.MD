[![Actions Status](https://github.com/Hakky54/certificate-ripper/workflows/Build/badge.svg)](https://github.com/Hakky54/certificate-ripper/actions)

# Certificate Ripper 🔐
A tool to extract server certificates

**Minimum requirements:**
1. Java 8
2. Maven 3.5.0
3. A terminal

## Usage
### Extract certificates as x509 format
#### Run with an executable jar
```text
mvn clean package
java -jar target/certificate-ripper.jar url=https://stackoverflow.com/
```
![alt text](https://github.com/Hakky54/certificate-ripper/blob/master/images/demo.gif?raw=true)

#### Run with a maven plugin
```text
mvn exec:java -Dexec.args="url=https://stackoverflow.com/" 
```

### Extract certificates as pem format
#### Run with an executable jar
```text
java -jar target/certificate-ripper.jar url=https://stackoverflow.com/ format=pem
```
#### Run with a maven plugin
```text
mvn exec:java -Dexec.args="url=https://stackoverflow.com/ format=pem" 
```