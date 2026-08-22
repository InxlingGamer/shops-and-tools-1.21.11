# Celestium 1.21.1 Validation

Validation date: 2026-08-22

Branch: `mc-1.21.1`

Release line: `celestium-1.21.1-1.0.1`
Status: Windows build, production-JAR audit, dedicated server, and the actual 221-mod CurseForge startup/world-join checks pass. Ubuntu CI and the final human elytra visual matrix are required before publishing a GitHub release or tag.

## 1.0.1 startup fixes

The 1.0.0 production JAR contained both Loom-generated refmaps, but all mixins were registered through `celestium.mixins.json`. Loom injected `celestium-1.21.1-refmap.json` into that common config, so the ten client mixins could not use `client-celestium-1.21.1-refmap.json`. Development launches hid the defect because development mappings were available at runtime.

- `celestium.mixins.json` now contains only the 16 common/server mixins.
- `celestium.client.mixins.json` contains all ten client mixins under `net.inklinggamer.celestium.mixin.client`.
- `fabric.mod.json` registers the client config with `"environment": "client"`.
- The remapped JAR uses `celestium-1.21.1-refmap.json` for the common config and `client-celestium-1.21.1-refmap.json` for the client config.
- `CelestiumProductionMixinRefmapTest` opens the final remapped JAR and verifies both configs/refmaps, every client mixin, and the 1.21.1 mappings for `doItemUse`, `doAttack`, `isHoldingOntoLadder`, `drawForeground`, and the fused-elytra redirect.

The first corrected-refmap launch exposed a separate Lithium incompatibility. Both Lithium and Celestium redirected the same `ServerWorld.tickChunk` random-tick invocation; Celestium won, then Lithium failed its required injection. Celestium now injects immediately after the vanilla random tick and captures the position/state locals through MixinExtras. The crop-growth aura and extra random ticks remain enabled without competing with Lithium's redirect. `CelestiumHoeFeatureTest` prevents the conflicting redirect from returning.

## Elytra regression

- `ArmorFeatureRendererMixin` remains removed, so the fused item no longer suppresses the Celestium torso/arm armor layer.
- `ElytraFeatureRendererMixin` remains in the client-only config, so the fused chestplate is accepted wherever vanilla expects `Items.ELYTRA`.
- `CelestiumChestItem` remains an `ArmorItem` using `ArmorItem.Type.CHESTPLATE`.
- `CelestiumElytraRenderingTest` requires both armor textures, the client-config elytra registration, and the redirect; it rejects any armor-suppression mixin.
- The automated layering regression passes. Standing, crouching, takeoff, sustained flight, inventory, glint, and durability still require a human visual pass because Windows Graphics Capture fails for this Java window with `SetIsBorderRequired failed: No such interface supported (0x80004002)`.

## Toolchain and contracts

- Minecraft `1.21.1`
- Java `21`
- Yarn `1.21.1+build.3`
- Fabric Loader `0.19.3`
- Fabric API `0.116.15+1.21.1`
- Fabric Loom `1.11.7`
- Gradle `8.14.3`
- Mod ID and production namespaces: `celestium`
- Fabric API is required; Iris is optional.
- `fabric.mod.json` requires Minecraft exactly `=1.21.1`, so the side JAR is rejected on Minecraft 26.2.

## Automated Windows validation

Command:

```powershell
.\gradlew.bat clean test build verifyProductionMixinRefmaps --no-daemon
```

Result: PASS. All feature/regression executables and Gradle's verification/build lifecycle completed. Coverage includes advancements, block/item assets, armor and light behavior, boots and wall climbing, elytra layering, hoe behavior/Lithium compatibility, horse armor, Iris helpers, leggings flight, metadata, pickaxe/X-ray modes, shovel modes/slam, smithing/anvil fusion, trim tags, Warden combat/boss bar, world-version rejection, experience bonuses, and final-JAR refmaps.

Additional checks:

- PASS: 89 JSON files across generated, main-resource, and client-resource roots parsed successfully.
- PASS: exactly 77 generated gameplay files exist, excluding Fabric's `.cache` metadata.
- PASS: two consecutive datagen runs produced identical gameplay files.
- PASS: no client API import exists under `src/main/java`.
- PASS: the final JAR has 324 entries, both mixin configs, both refmaps, one elytra mixin, no legacy namespace entry, and no armor-suppression mixin.
- PASS: the production refmap audit verifies `doItemUse -> method_1583`, `doAttack -> method_1536`, `isHoldingOntoLadder -> method_21754`, `drawForeground -> method_2388`, and `ItemStack.isOf -> method_31574`.

Dedicated server command:

```powershell
.\gradlew.bat runServer --no-daemon --args nogui
```

Result: PASS. Minecraft 1.21.1 with Celestium 1.0.1 reached `Done` on the disposable port 25566, then saved all three dimensions and stopped cleanly.

## Actual CurseForge validation

Instance: `C:\Users\Louie\curseforge\minecraft\Instances\Cobblemon Server`

- Moved `celestium-1.21.1-1.0.0.jar` out of `mods` to `celestium-backups\celestium-1.21.1-1.0.0.jar`.
- Installed only `celestium-1.21.1-1.0.1.jar` in `mods`.
- Backup 1.0.0 SHA-256: `5B2D33654AD8B9F5423BAAFD7E8ECD87CCE1E9A28574B8B225F5F7CB86F2339A`.
- Final installed 1.0.1 SHA-256 matches the build artifact: `1DB115FE5E2E87F6FCB0044F9158453B8381AEC3DAD58C94478E2FE61E8613E1`.
- CurseForge loaded Minecraft 1.21.1, Fabric Loader 0.19.3, and all 221 mods, including Celestium 1.0.1, Iris 1.8.8, Sodium 0.6.13, and Lithium 0.15.4.
- PASS: the previous `LivingEntityClientMixin`, `MinecraftClientMixin`, `doItemUse`, and `isHoldingOntoLadder` failures are absent.
- PASS: the final launch has no Celestium failed mixin and no missing Celestium asset.
- PASS: audio initialized, all texture atlases were created, and the title screen completed startup in 54.835 seconds.
- PASS: created a clean `New World`, started the integrated Minecraft 1.21.1 server, generated all three dimensions, and logged `InxlingGamer joined the game`.
- Runtime log: `C:\Users\Louie\curseforge\minecraft\Instances\Cobblemon Server\logs\latest.log`.
- Preliminary compatibility crash retained as evidence: `C:\Users\Louie\curseforge\minecraft\Instances\Cobblemon Server\crash-reports\crash-2026-08-22_01.39.06-client.txt`.

Unrelated pack warnings include optional absent-mod mixin targets, other mods' missing refmaps, Cobblemon/Biomes O' Plenty data-fixer notices, a Veinminer datapack error, and a Complementary/Iris uniform warning for post-1.21.1 shader variables. None is a Celestium failed mixin, missing Celestium resource, or startup failure.

## Version isolation

- Prior disposable 26.2 validation confirmed Loader rejects the 1.21.1 JAR with `HARD_DEP_INCOMPATIBLE_PRESELECTED` because Celestium requires Minecraft 1.21.1.
- If both Celestium JARs are physically present, Fabric treats them as candidates for the canonical `celestium` ID; only the compatible candidate can activate. Users must install only the JAR matching their Minecraft version.
- `CelestiumWorldVersionGuardTest` verifies that saves with a newer `DataVersion` are rejected before session creation.

## Artifacts

- `celestium-1.21.1-1.0.1.jar`
  - SHA-256: `1DB115FE5E2E87F6FCB0044F9158453B8381AEC3DAD58C94478E2FE61E8613E1`
- `celestium-1.21.1-1.0.1-sources.jar`
  - SHA-256: `AB33AA214731E7486291F7C04FCC8E88E8D9625B9CAFA4FE1BF45D0A819FA990`

## Preserved 26.2 release

- Main repository HEAD: `a7f82664cbaa429e507f9540e8cd0fc20956a2b0`
- Main repository working tree: clean
- Original `celestium-1.1.1.jar` SHA-256: `38A42F562944FF5FE93D81942A61214026C0E258D14C09CBBBC1A5B13C6DEA8E`
- Preserved copy SHA-256: `38A42F562944FF5FE93D81942A61214026C0E258D14C09CBBBC1A5B13C6DEA8E`
- `C:\Users\Louie\IdeaProjects\shops-and-tools-1.21.11` contains no non-`.git` files and was not used.

## Ubuntu CI

The `mc-1.21.1` workflow uses Ubuntu 24.04 and Java 21. It runs clean tests, deterministic 77-file datagen, namespace/source-set/89-JSON audits, release/source builds, the final remapped-JAR refmap audit, exact artifact-name checks, and JAR content checks.

Current 1.0.1 CI result: pending push. Do not create a GitHub release or tag until this section records a passing run and the human elytra visual matrix is complete.
