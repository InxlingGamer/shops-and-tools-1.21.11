package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record SyncCelestiumTrialChamberMarkerPayload(BlockPos pos, Identifier dimensionId, int durationTicks) implements CustomPayload {
    public static final Id<SyncCelestiumTrialChamberMarkerPayload> ID =
            new Id<>(Identifier.of(Celestium.MOD_ID, "sync_celestium_trial_chamber_marker"));
    public static final PacketCodec<PacketByteBuf, SyncCelestiumTrialChamberMarkerPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeBlockPos(payload.pos());
                buf.writeIdentifier(payload.dimensionId());
                buf.writeVarInt(payload.durationTicks());
            },
            buf -> new SyncCelestiumTrialChamberMarkerPayload(
                    buf.readBlockPos(),
                    buf.readIdentifier(),
                    buf.readVarInt()
            )
    );

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }

    public static void send(ServerPlayerEntity player, BlockPos pos, Identifier dimensionId, int durationTicks) {
        ServerPlayNetworking.send(player, new SyncCelestiumTrialChamberMarkerPayload(pos, dimensionId, durationTicks));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
