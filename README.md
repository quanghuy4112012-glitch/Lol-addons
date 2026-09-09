# LOL Addon

Clean-room Meteor addon source targeting Minecraft 1.21.11.

## Included modules

- `LOL Chunk Finder`
- `Tuff Chunk Finder`

The two modules are intentionally implemented from scratch rather than copying the obfuscated/cracked classes from third-party JARs.

## Build

Use Java 21 and the Gradle wrapper from the official Meteor addon template, then run:

```bash
./gradlew build
```

The built JAR will appear under `build/libs/`.

## Important

The exact versions in `gradle.properties` may need to be adjusted to the Meteor/Fabric snapshot you have installed. The official Meteor addon template documents this workflow and version catalog approach.
