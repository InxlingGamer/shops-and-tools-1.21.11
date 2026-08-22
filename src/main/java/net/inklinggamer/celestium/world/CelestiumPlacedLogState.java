package net.inklinggamer.celestium.world;

import net.inklinggamer.celestium.Celestium;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashSet;
import java.util.Set;

public final class CelestiumPlacedLogState extends PersistentState {
    private static final String ID = Celestium.MOD_ID + "_placed_logs";
    private static final String POSITIONS_KEY = "Positions";
    private static final Type<CelestiumPlacedLogState> TYPE =
            new Type<>(CelestiumPlacedLogState::new, CelestiumPlacedLogState::fromNbt, DataFixTypes.LEVEL);

    private final Set<Long> playerPlacedLogs = new HashSet<>();

    public static CelestiumPlacedLogState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, ID);
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

    private static CelestiumPlacedLogState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        CelestiumPlacedLogState state = new CelestiumPlacedLogState();
        for (long encodedPosition : nbt.getLongArray(POSITIONS_KEY)) {
            state.playerPlacedLogs.add(encodedPosition);
        }
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putLongArray(POSITIONS_KEY, this.playerPlacedLogs.stream().mapToLong(Long::longValue).toArray());
        return nbt;
    }
}
