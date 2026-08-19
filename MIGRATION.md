# Celestium 1.1.1 migration notes

Celestium 1.1.1 targets Minecraft Java 26.2 and requires Java 25, Fabric Loader 0.19.3 or newer, and Fabric API 0.158.0+26.2. It fixes a client startup failure caused by the Minecraft 26.2 anvil screen method rename and does not change gameplay, registries, networking, or saved data.

## Before upgrading

1. Stop the old server or close the world normally.
2. Make a complete backup of the world and player data.
3. Install matching Celestium 1.1.1 builds on the server and every client.
4. Keep the backup until all important dimensions, chunks, containers, and entity inventories have been visited on 1.1.x.

## Compatibility behavior

The former `shopsandtools` dependency ID is provided as an alias during 1.1.x. Saved registry IDs are handled separately by a hidden bridge:

- All legacy item IDs and the legacy Celestium block remain registered but are excluded from creative tabs and new content.
- Item stacks convert to `celestium:*` while preserving count and the complete component patch, including damage, enchantments, names, lore, and custom data.
- Player inventories, armor, offhand, ender chests, open menus, loaded containers, dropped items, equipment, and mount inventories are migrated when loaded.
- Loaded chunk palettes replace the legacy Celestium block with `celestium:celestium_block`.
- Legacy transient attribute IDs are removed before canonical modifiers are applied, preventing duplicate bonuses.
- Legacy advancement criteria and recipe-book discoveries are transferred to their canonical IDs without re-awarding advancement rewards.
- Migration version and conversion totals are stored in `celestium:migration_state`.

The bridge is idempotent and remains part of every 1.1.x build. Removing it is deferred to a later breaking release with a dedicated full-world converter.

## Network compatibility

All custom payload channels now use `celestium:*`. A 1.21.11 client is not network-compatible with a Minecraft 26.2 server.

## Validation checklist

- Confirm the server reaches `Done` without failed mixins, registry sync failures, or invalid data entries.
- Inspect representative player, container, dropped-item, mount, and block fixtures.
- Reload the same fixtures and confirm that no items, progress, rewards, or attribute bonuses duplicate.
- Exercise equipment, tools, Warden progression, screens, HUD elements, and world overlays in singleplayer and dedicated multiplayer.
