# wattpilot4j - Publish

## Setup

### GPG Signing

One of the requirements for publishing to Maven Central is to sign your artefacts with PGP.
See <https://central.sonatype.org/publish/requirements/gpg/> to learn how to set up GnuPG, generate a key pair and distributing the key.

You need to configure the Maven GPG plugin to use the correct key.
Get the key id using the following command:

```shell
gpg --list-keys --keyid-format short
```

Then, configure in `~/.m2/settings.xml`:

```xml
<settings>
  <profiles>
    <profile>
      <id>central</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg2</gpg.executable>
        <gpg.keyname>THE_KEY_ID</gpg.keyname>
      </properties>
    </profile>
  </profiles>
</settings>
```

Use the `local-sign` Maven profile to locally sign the build artifacts:

```shell
./mvnw clean package -P local-sign
```

### Configure Maven Central Repository Credentials

Follow <https://central.sonatype.org/publish/publish-portal-maven/#credentials> to set up the maven central repository credentials.

## Publish to Maven Central Repository

Create a new release:

```shell
./mvnw release:prepare
./mvnw release:clean
```

Checkout the Git tag of the release:

```shell
git checkout vX.Y.Z
```

Then publish through the following command:

```shell
./mvnw clean deploy -P central
```

The output will provide a link to <https://central.sonatype.com/publishing/deployments>, where you can publish the deployment.
