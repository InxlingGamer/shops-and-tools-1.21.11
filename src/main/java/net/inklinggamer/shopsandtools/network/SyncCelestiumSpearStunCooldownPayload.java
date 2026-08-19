package net.inklinggamer.shopsandtools.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.client.CelestiumSpearStunCooldownHud;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record SyncCelestiumSpearStunCooldownPayload(int remainingTicks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncCelestiumSpearStunCooldownPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "sync_celestium_spear_stun_cooldown"));
    public static final StreamCodec<FriendlyByteBuf, SyncCelestiumSpearStunCooldownPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SyncCelestiumSpearStunCooldownPayload::remainingTicks,
            SyncCelestiumSpearStunCooldownPayload::new
    );

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(ID, CODEC);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.client().execute(() -> CelestiumSpearStunCooldownHud.syncCooldown(payload.remainingTicks()))
        );
    }

    public static void send(ServerPlayer player, int remainingTicks) {
        ServerPlayNetworking.send(player, new SyncCelestiumSpearStunCooldownPayload(remainingTicks));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
