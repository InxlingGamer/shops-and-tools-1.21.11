package net.inklinggamer.shopsandtools.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.client.CelestiumThrustCooldownHud;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record SyncCelestiumThrustCooldownPayload(int remainingTicks) implements CustomPayload {
    public static final CustomPayload.Id<SyncCelestiumThrustCooldownPayload> ID =
            new CustomPayload.Id<>(Identifier.of(ShopsAndTools.MOD_ID, "sync_celestium_thrust_cooldown"));
    public static final PacketCodec<PacketByteBuf, SyncCelestiumThrustCooldownPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            SyncCelestiumThrustCooldownPayload::remainingTicks,
            SyncCelestiumThrustCooldownPayload::new
    );

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.client().execute(() -> CelestiumThrustCooldownHud.syncCooldown(payload.remainingTicks()))
        );
    }

    public static void send(ServerPlayerEntity player, int remainingTicks) {
        ServerPlayNetworking.send(player, new SyncCelestiumThrustCooldownPayload(remainingTicks));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
