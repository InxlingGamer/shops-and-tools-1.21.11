package net.inklinggamer.shopsandtools.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.client.CelestiumRageHud;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record SyncCelestiumRagePayload(int stacks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncCelestiumRagePayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "sync_celestium_rage"));
    public static final StreamCodec<FriendlyByteBuf, SyncCelestiumRagePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SyncCelestiumRagePayload::stacks,
            SyncCelestiumRagePayload::new
    );

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.client().execute(() -> CelestiumRageHud.syncStacks(payload.stacks()))
        );
    }

    public static void send(ServerPlayer player, int stacks) {
        ServerPlayNetworking.send(player, new SyncCelestiumRagePayload(stacks));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
