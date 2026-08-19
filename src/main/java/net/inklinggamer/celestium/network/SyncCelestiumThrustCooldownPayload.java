package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record SyncCelestiumThrustCooldownPayload(int remainingTicks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncCelestiumThrustCooldownPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "sync_celestium_thrust_cooldown"));
    public static final StreamCodec<FriendlyByteBuf, SyncCelestiumThrustCooldownPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SyncCelestiumThrustCooldownPayload::remainingTicks,
            SyncCelestiumThrustCooldownPayload::new
    );

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(ID, CODEC);
    }


    public static void send(ServerPlayer player, int remainingTicks) {
        ServerPlayNetworking.send(player, new SyncCelestiumThrustCooldownPayload(remainingTicks));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
