package net.inklinggamer.shopsandtools.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.client.CelestiumShovelClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record SyncCelestiumTrialChamberMarkerPayload(BlockPos pos, Identifier dimensionId, int durationTicks) implements CustomPayload {
    public static final Id<SyncCelestiumTrialChamberMarkerPayload> ID =
            new Id<>(Identifier.of(ShopsAndTools.MOD_ID, "sync_celestium_trial_chamber_marker"));
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

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.client().execute(() ->
                        CelestiumShovelClient.syncTrialChamberMarker(payload.pos(), payload.dimensionId(), payload.durationTicks()))
        );
    }

    public static void send(ServerPlayerEntity player, BlockPos pos, Identifier dimensionId, int durationTicks) {
        ServerPlayNetworking.send(player, new SyncCelestiumTrialChamberMarkerPayload(pos, dimensionId, durationTicks));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
