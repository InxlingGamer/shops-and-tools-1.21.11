package net.inklinggamer.shopsandtools.world;

import com.mojang.serialization.Codec;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CelestiumPlacedLogState extends SavedData {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "placed_logs");
    private static final Codec<CelestiumPlacedLogState> CODEC = Codec.LONG.listOf().xmap(
            CelestiumPlacedLogState::fromEncodedPositions,
            state -> state.playerPlacedLogs.stream().toList()
    );
    private static final SavedDataType<CelestiumPlacedLogState> TYPE =
            new SavedDataType<>(ID, CelestiumPlacedLogState::new, CODEC, DataFixTypes.LEVEL);

    private final Set<Long> playerPlacedLogs = new HashSet<>();

    public static CelestiumPlacedLogState get(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isPlayerPlaced(BlockPos pos) {
        return this.playerPlacedLogs.contains(pos.asLong());
    }

    public void markPlaced(BlockPos pos) {
        if (this.playerPlacedLogs.add(pos.asLong())) {
            this.setDirty();
        }
    }

    public void unmark(BlockPos pos) {
        if (this.playerPlacedLogs.remove(pos.asLong())) {
            this.setDirty();
        }
    }

    private static CelestiumPlacedLogState fromEncodedPositions(List<Long> encodedPositions) {
        CelestiumPlacedLogState state = new CelestiumPlacedLogState();
        state.playerPlacedLogs.addAll(encodedPositions);
        return state;
    }
}
