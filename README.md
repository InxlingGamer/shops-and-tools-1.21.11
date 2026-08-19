# Celestium

Celestium is a Fabric mod that adds a late-game material, equipment set, tools, Warden progression, and supporting combat and utility mechanics.

## Requirements

- Minecraft Java 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API 0.158.0+26.2
- Java 25

Client and server installations must use the same Celestium 1.1.x build. Older Minecraft clients cannot connect to a 26.2 server.

## Building

Use JDK 25 and run:

```text
gradlew clean test runDatagen build identityAudit
```

The release and sources archives are written to `build/libs`.

## Updating an existing world

Back up the world before opening it in Minecraft 26.2. Celestium 1.1.x registers a hidden compatibility layer for content saved by the previous release and converts legacy item stacks, loaded inventories, entities, and placed blocks as they are encountered. The bridge stays enabled throughout the 1.1.x line because unloaded chunks may still contain legacy content.

See [MIGRATION.md](MIGRATION.md) for the full upgrade contract and validation checklist.

## License

Celestium is available under the MIT License.
