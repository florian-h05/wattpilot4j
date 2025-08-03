# wattpilot4j

`wattpilot4j` is Java (>= 17) client library to interact with [Fronius Wattpilot wallboxes](https://www.fronius.com/en-gb/uk/solar-energy/home-owners/products-and-solutions/e-mobility/wattpilot-electric-car-charger-for-homes), which unfortunately don't provide an official API.
This client library is based on the undocumented WebSockets API, which is used by the official Wattpilot.Solar mobile app.
Check out wattpilot4j‘s [DeepWiki](https://deepwiki.com/florian-h05/wattpilot4j) for documentation.

This implementation is based on the API documentation at [joscha82/wattpilot](https://github.com/joscha82/wattpilot).
Many thanks for the great work!

## Wattpilot Client Library

wattpilot4j provides a simple Java API to interact with the Wattpilot wallbox.

### Maven Dependency

It is available on Maven Central, so you simply add it to your project by adding the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.digiried</groupId>
    <artifactId>wattpilot4j</artifactId>
    <version>VERSION</version>
</dependency>
```

Replace `VERSION` with a version available on [Maven Central](https://repo1.maven.org/maven2/dev/digiried/wattpilot4j/).

All releases are signed with the PGP key `F2CB9FE8FD59D83C0816FF0165DED02BE8FA21A6`, you can find the public key on [keys.openpgp.org](https://keys.openpgp.org/search?q=F2CB9FE8FD59D83C0816FF0165DED02BE8FA21A6).
To import the public key, use the following command:

```shell
gpg --keyserver keys.openpgp.org --recv-keys F2CB9FE8FD59D83C0816FF0165DED02BE8FA21A6
```

### Usage

You only need to implement the [`WattpilotListener`](src/main/java/dev/digiried/wattpilot/WattpilotClientListener.java) interface to receive connection and disconnect events, as well as status updates,
and create an instance of the [`WattpilotClient`](src/main/java/dev/digiried/wattpilot/WattpilotClient.java) class, add your listener and connect to the wallbox through the `connect` method.

As an example, have a look at the [main class of the Wattpilot shell](src/main/java/dev/digiried/wattpilot/shell/App.java)
or the [openHAB Wattpilot Binding](https://github.com/openhab/openhab-addons/tree/main/bundles/org.openhab.binding.froniuswattpilot).

### OSGi

wattpilot4j also includes an OSGi manifest, so it can be deployed as its own bundle in OSGi environments like Apache Karaf.

## Wattpilot Shell

The shell provides an easy way to interact with the Wattpilot wallbox without writing any code.
It allows to get the status and control some charging settings such as mode, current and power threshold.

Compile and run the shell with:

```shell
./mvnw clean compile exec:java -P shell "-Dexec.args=YOUR_WALLBOX_IP YOUR_WALLBOX_PASSWORD"
```

You can optionally configure logging by providing `org.slf4j.simpleLogger.log` system properties on the command line, e.g.:

```
-Dorg.slf4j.simpleLogger.defaultLogLevel=info
-Dorg.slf4j.simpleLogger.log.dev.digiried=debug
```

## Disclaimer

This project is not affiliated with, endorsed by, or supported by Fronius International GmbH.
"Fronius" and "Wattpilot" are trademarks or registered trademarks of Fronius International GmbH.
All other trademarks and brand names mentioned in this project are the property of their respective owners.
