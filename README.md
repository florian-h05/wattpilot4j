# wattpilot4j

`wattpilot4j` is Java (>= 17) client library to interact with [Fronius Wattpilot wallboxes](https://www.fronius.com/en-gb/uk/solar-energy/home-owners/products-and-solutions/e-mobility/wattpilot-electric-car-charger-for-homes), which unfortunately don't provide an official API.
This client library is based on the undocumented WebSockets API, which is used by the the official Wattpilot.Solar mobile app.

This implementation is based on the API documentation at [joscha82/wattpilot](https://github.com/joscha82/wattpilot).
Many thanks for the great work!

## Wattpilot Shell

The shell provides an easy way to interact with the Wattpilot wallbox without writing any code.
It allows to get the status and control some charging settings such as mode, current and power threshold.

Compile and run the shell with:

```shell
./mvnw clean compile exec:java -P shell "-Dexec.args=YOUR_WALLBOX_IP YOUR_WALLBOX_PASSWORD"
```

## Disclaimer
This project is not affiliated with, endorsed by, or supported by Fronius International GmbH.
"Fronius" and "Wattpilot" are trademarks or registered trademarks of Fronius International GmbH.
All other trademarks and brand names mentioned in this project are the property of their respective owners.
