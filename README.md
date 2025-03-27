# wattpilot4j

`wattpilot4j` is Java (>= 17) library to interact with Fronius Wattpilot wallboxes, which unfortunately don't provide an official API.
This library is based on the undocumented WebSockets API, which is used by the the official Wattpilot.Solar mobile app.

This implementation is based on the API documentation at [joscha82/wattpilot](https://github.com/joscha82/wattpilot).
Many thanks for the great work!

## Wattpilot Shell

The shell provides an easy way to interact with the Wattpilot wallbox without writing any code.
It allows to get the status and control some charging settings such as mode, current and power threshold.

Compile the shell with:

```shell
./mvnw clean compile assembly:single
```

Run the shell with:

```shell
java -cp target/wattpilot4j-*-jar-with-dependencies.jar com.florianhotze.wattpilot.shell.App YOUR_WALLBOX_IP YOUR_WALLBOX_PASSWORD
```
