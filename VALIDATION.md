# Celestium 1.1.1 validation summary

Validated on Windows with Java 25, Gradle 9.5.1, Minecraft 26.2, Fabric Loader 0.19.3, Loom 1.17.19, and Fabric API 0.158.0+26.2.

## Automated checks

- Clean compilation of common, client, test, and sources artifacts
- All existing feature verification programs and the Minecraft-bytecode mixin audit pass
- Migration regression program passes
- Canonical metadata, entrypoint, registry, generated namespace, and all 12 packet-channel checks pass
- Identity allowlist audit passes
- Datagen completes with 99 files and a second pass writes zero files with an identical aggregate digest
- Final release and sources archives build successfully

## Runtime checks

- Client reaches a complete OpenGL resource reload with no failed mixins, missing models, missing textures, or invalid data entries
- The production CurseForge profile reaches the title screen after CreativeCore eagerly initializes menu screens
- The client anvil mixin resolves exactly once against Minecraft 26.2 `extractLabels` and preserves numeric costs above 40
- Dedicated server reaches `Done` with client code isolated from the common source set
- All canonical and compatibility recipes and advancements load successfully
- A saved legacy item entity converts to `celestium:celestium` while retaining its count
- A saved legacy block palette entry converts to `celestium:celestium_block`
- A second server load performs no additional conversions and preserves both canonical results, demonstrating idempotence

## Artifacts

- `build/libs/celestium-1.1.1.jar`
- `build/libs/celestium-1.1.1-sources.jar`
