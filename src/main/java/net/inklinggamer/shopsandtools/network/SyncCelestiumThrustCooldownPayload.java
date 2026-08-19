package net.inklinggamer.shopsandtools.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.client.CelestiumThrustCooldownHud;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record SyncCelestiumThrustCooldownPayload(int remainingTicks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncCelestiumThrustCooldownPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "sync_celestium_thrust_cooldown"));
    public static final StreamCodec<FriendlyByteBuf, SyncCelestiumThrustCooldownPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SyncCelestiumThrustCooldownPayload::remainingTicks,
            SyncCelestiumThrustCooldownPayload::new
    );

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(ID, CODEC);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.client().execute(() -> CelestiumThrustCooldownHud.syncCooldown(payload.remainingTicks()))
        );
    }

    public static void send(ServerPlayer player, int remainingTicks) {
        ServerPlayNetworking.send(player, new SyncCelestiumThrustCooldownPayload(remainingTicks));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
