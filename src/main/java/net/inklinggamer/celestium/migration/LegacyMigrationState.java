package net.inklinggamer.celestium.migration;

import com.mojang.serialization.Codec;
import net.inklinggamer.celestium.Celestium;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.List;

public final class LegacyMigrationState extends SavedData {
    private static final int CURRENT_VERSION = 1;
    private static final Identifier ID = Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "migration_state");
    private static final Codec<LegacyMigrationState> CODEC = Codec.LONG.listOf().xmap(
            LegacyMigrationState::decode,
            state -> List.of((long) state.version, state.convertedItems, state.convertedBlocks, state.warningLogged ? 1L : 0L)
    );
    private static final SavedDataType<LegacyMigrationState> TYPE =
            new SavedDataType<>(ID, LegacyMigrationState::new, CODEC, DataFixTypes.LEVEL);

    private int version = CURRENT_VERSION;
    private long convertedItems;
    private long convertedBlocks;
    private boolean warningLogged;

    public static LegacyMigrationState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void logFirstRunWarning() {
        if (this.warningLogged) {
            return;
        }

        Celestium.LOGGER.warn("Legacy world compatibility is active. Keep a backup until every old chunk and inventory has been loaded and converted.");
        this.warningLogged = true;
        this.setDirty();
    }

    public void record(long items, long blocks) {
        if (items == 0 && blocks == 0) {
            return;
        }

        long previousTotal = this.convertedItems + this.convertedBlocks;
        this.convertedItems += items;
        this.convertedBlocks += blocks;
        this.version = CURRENT_VERSION;
        this.setDirty();

        long total = this.convertedItems + this.convertedBlocks;
        if (previousTotal == 0 || previousTotal / 100 != total / 100) {
            Celestium.LOGGER.info("Legacy migration totals: {} item stacks and {} blocks converted", this.convertedItems, this.convertedBlocks);
        }
    }

    public long convertedItems() {
        return this.convertedItems;
    }

    public long convertedBlocks() {
        return this.convertedBlocks;
    }

    private static LegacyMigrationState decode(List<Long> values) {
        LegacyMigrationState state = new LegacyMigrationState();
        if (!values.isEmpty()) {
            state.version = values.get(0).intValue();
        }
        if (values.size() > 1) {
            state.convertedItems = values.get(1);
        }
        if (values.size() > 2) {
            state.convertedBlocks = values.get(2);
        }
        if (values.size() > 3) {
            state.warningLogged = values.get(3) != 0;
        }
        return state;
    }
}
