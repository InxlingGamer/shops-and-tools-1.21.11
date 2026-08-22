# Celestium 1.21.1 Validation

Validation date: 2026-08-21  
Branch: `mc-1.21.1`  
Release line: `celestium-1.21.1-1.0.0`  
Status: branch candidate; no release tag has been created.

## Elytra regression

- Removed `ArmorFeatureRendererMixin`, which replaced the fused chestplate with air during armor rendering.
- Kept `ElytraFeatureRendererMixin`, so the fused item is accepted wherever vanilla expects `Items.ELYTRA`.
- Kept `CelestiumChestItem` as an `ArmorItem` using `ArmorItem.Type.CHESTPLATE`, allowing the normal torso and arm armor layer to render.
- Added `CelestiumElytraRenderingTest`. It requires the armor item and both armor textures, requires the elytra redirect, and fails if the armor-suppression mixin or registration returns.
- The automated regression checks pass. The final standing, crouching, takeoff, sustained-flight, glint, and inventory visual matrix still requires a human screenshot pass because Windows Graphics Capture failed for the Java game window with `SetIsBorderRequired failed: No such interface supported (0x80004002)`.

## Toolchain and contracts

- Minecraft `1.21.1`
- Java `21`
- Yarn `1.21.1+build.3`
- Fabric Loader `0.19.3`
- Fabric API `0.116.15+1.21.1`
- Fabric Loom `1.11.7`
- Gradle `8.14.3`
- Mod ID and all registries/resources/packets: `celestium`
- Iris is optional; Fabric API is required.
- `fabric.mod.json` requires Minecraft exactly `=1.21.1`, so the side JAR is not accepted by Minecraft 26.2.

## Automated Windows validation

Command:

```powershell
.\gradlew.bat clean test build --no-daemon
```

Result: PASS. The build ran 18 feature/regression executables and Gradle's normal verification/build lifecycle. Coverage includes advancements, block/item assets, armor and light behavior, boots and wall climbing, elytra layering, hoe, horse armor, Iris compatibility helpers, leggings flight, release metadata, pickaxe modes/X-ray helpers, shovel modes/slam, smithing/anvil fusion, trim tags, Warden combat/boss bar, world-version rejection, and experience bonuses.

Other checks:

- PASS: 88 JSON asset/data/metadata files parsed successfully.
- PASS: 77 generated gameplay files are present, excluding Fabric `.cache` metadata.
- PASS: two consecutive datagen runs produced zero differences.
- Datagen aggregate SHA-256: `6981C1B50E6A0E7C48146C4FF5C81F855CCDA4E5D7D5F77FDF1222069FF4053A`
- PASS: no `shopsandtools` or `shops-and-tools` namespace remains.
- PASS: no client API import exists under `src/main/java`.
- PASS: no audited post-1.21.1 equipment/item-model API signature remains.
- PASS: client datagen and real client startup applied all client mixins without a failed Celestium mixin.
- PASS: dedicated server startup applied common mixins without a failed Celestium mixin.
- PASS: release JAR contains 323 entries, no legacy entries, no `ArmorFeatureRendererMixin`, and one `ElytraFeatureRendererMixin`.

## Runtime evidence

Vanilla/Fabric client:

- Loaded Minecraft 1.21.1, Celestium 1.0.0, and Fabric API 0.116.15+1.21.1.
- Loaded 1,306 recipes and 1,422 advancements.
- Started and joined the existing clean 1.21.1 `Test` world.
- Celestium advancement execution was observed (`Fully Ascended`).
- No Celestium missing asset, invalid data, failed mixin, or crash was logged.

Dedicated server:

- An unrelated existing server occupied port 25565, so this disposable server used port 25566.
- Reached `Done`, reloaded 1,306 recipes and 1,422 advancements, saved all three dimensions, and stopped cleanly.

Iris/Sodium:

- Iris `1.8.8+mc1.21.1`, SHA-256 `B5AD39A6CB113CA0A1765F06689F9F4B29CCD0C81247DE90B06C42F5DA1092F5`
- Sodium `0.6.13+mc1.21.1`, SHA-256 `E04599514D88E41765F710EEBA59E3814909833396BC11B977F48FC2E7E50353`
- Complementary Reimagined `r5.7.1`, SHA-256 `24A20634A7832D422D3CD5023BE16829F26840E25C69404DD418306EA79F63F0`
- PASS: Iris and Sodium loaded, Celestium entered the integrated world, and Celestium's Complementary/Iris pearlescent froglight material alias activated.
- External fixture warning: Complementary r5.7.1 references `BIOME_PALE_GARDEN`, which is not present in Minecraft 1.21.1. Iris logs a non-fatal uniform-resolution warning and continues. This is not a Celestium mixin or asset failure.

## Version isolation

A disposable Minecraft 26.2/Fabric Loader 0.19.3/Java 25 profile was built outside both repositories with Loom 1.17.19.

- PASS: installing only `celestium-1.21.1-1.0.0.jar` causes Loader to reject startup with `HARD_DEP_INCOMPATIBLE_PRESELECTED` and the explicit message that Celestium 1.0.0 requires Minecraft 1.21.1 but 26.2 is present.
- PASS: when both Celestium JAR files are placed in the 26.2 profile, Loader activates only the compatible 26.2 candidate (`celestium 1.1.1`); the 1.21.1 candidate is not loaded.
- Constraint: because both releases intentionally use the canonical `celestium` mod ID, Fabric treats them as candidates for one mod rather than hard-failing solely because both files exist. They can never both be active, but users must still install only the version matching their Minecraft profile.
- PASS: `CelestiumWorldVersionGuardTest` verifies that saves with a newer `DataVersion` are rejected before session creation.

## Artifacts

- `celestium-1.21.1-1.0.0.jar`
  - SHA-256: `5B2D33654AD8B9F5423BAAFD7E8ECD87CCE1E9A28574B8B225F5F7CB86F2339A`
- `celestium-1.21.1-1.0.0-sources.jar`
  - SHA-256: `46E5073D7E107AB0846B8646BF5255985F7679B553676BF3125BCE15972138F8`

## Preserved 26.2 release

- Main repository HEAD: `a7f82664cbaa429e507f9540e8cd0fc20956a2b0`
- Main repository working tree: clean
- Original `celestium-1.1.1.jar` SHA-256: `38A42F562944FF5FE93D81942A61214026C0E258D14C09CBBBC1A5B13C6DEA8E`
- Preserved copy SHA-256: `38A42F562944FF5FE93D81942A61214026C0E258D14C09CBBBC1A5B13C6DEA8E`
- `shops-and-tools-1.21.11` contains no working project files; only its pre-existing `.git` metadata remains, and it was not used for development or builds.

## Ubuntu CI

The `mc-1.21.1` workflow uses Ubuntu 24.04 and Java 21. It runs clean tests, deterministic datagen, the 77-file count, namespace/source-set/JSON audits, exact artifact-name checks, and JAR content checks. The run URL and final CI result are recorded in the follow-up commit after the branch is first pushed.

## Known non-Celestium warnings

- Minecraft development launch reports the empty generated `build/resources/client` source-set output path.
- Vanilla reports two missing goat-horn sound events and a `Sampler2` shader warning.
- Sodium reports its standard NVIDIA threaded-optimization workaround on this machine.
- Development remapping of the pinned Iris/Sodium production JARs reports three mapping warnings; the mods proceed to load.
- `CelestiumBootsManager` compiles against a deprecated 1.21.1 API. It compiles and its movement/sound regression tests pass.

No GitHub release or tag should be created until the remaining human visual matrix is marked PASS.
