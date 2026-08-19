package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record SyncCelestiumTrialChamberMarkerPayload(BlockPos pos, Identifier dimensionId, int durationTicks) implements CustomPacketPayload {
    public static final Type<SyncCelestiumTrialChamberMarkerPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "sync_celestium_trial_chamber_marker"));
    public static final StreamCodec<FriendlyByteBuf, SyncCelestiumTrialChamberMarkerPayload> CODEC = StreamCodec.ofMember(
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
        PayloadTypeRegistry.clientboundPlay().register(ID, CODEC);
    }


    public static void send(ServerPlayer player, BlockPos pos, Identifier dimensionId, int durationTicks) {
        ServerPlayNetworking.send(player, new SyncCelestiumTrialChamberMarkerPayload(pos, dimensionId, durationTicks));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
