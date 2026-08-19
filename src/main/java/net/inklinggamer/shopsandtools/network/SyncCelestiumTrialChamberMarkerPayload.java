package net.inklinggamer.shopsandtools.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.client.CelestiumShovelClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record SyncCelestiumTrialChamberMarkerPayload(BlockPos pos, Identifier dimensionId, int durationTicks) implements CustomPacketPayload {
    public static final Type<SyncCelestiumTrialChamberMarkerPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "sync_celestium_trial_chamber_marker"));
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
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.client().execute(() ->
                        CelestiumShovelClient.syncTrialChamberMarker(payload.pos(), payload.dimensionId(), payload.durationTicks()))
        );
    }

    public static void send(ServerPlayer player, BlockPos pos, Identifier dimensionId, int durationTicks) {
        ServerPlayNetworking.send(player, new SyncCelestiumTrialChamberMarkerPayload(pos, dimensionId, durationTicks));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
