package net.inklinggamer.shopsandtools.world;

import com.mojang.serialization.Codec;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CelestiumPlacedLogState extends PersistentState {
    private static final String ID = ShopsAndTools.MOD_ID + "_placed_logs";
    private static final Codec<CelestiumPlacedLogState> CODEC = Codec.LONG.listOf().xmap(
            CelestiumPlacedLogState::fromEncodedPositions,
            state -> state.playerPlacedLogs.stream().toList()
    );
    private static final PersistentStateType<CelestiumPlacedLogState> TYPE =
            new PersistentStateType<>(ID, CelestiumPlacedLogState::new, CODEC, DataFixTypes.LEVEL);

    private final Set<Long> playerPlacedLogs = new HashSet<>();

    public static CelestiumPlacedLogState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public boolean isPlayerPlaced(BlockPos pos) {
        return this.playerPlacedLogs.contains(pos.asLong());
    }

    public void markPlaced(BlockPos pos) {
        if (this.playerPlacedLogs.add(pos.asLong())) {
            this.markDirty();
        }
    }

    public void unmark(BlockPos pos) {
        if (this.playerPlacedLogs.remove(pos.asLong())) {
            this.markDirty();
        }
    }

    private static CelestiumPlacedLogState fromEncodedPositions(List<Long> encodedPositions) {
        CelestiumPlacedLogState state = new CelestiumPlacedLogState();
        state.playerPlacedLogs.addAll(encodedPositions);
        return state;
    }
}
