package net.inklinggamer.shopsandtools.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.player.CelestiumBootsManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncCelestiumWallClimbInputPayload(boolean sneakHeld, boolean forwardHeld, boolean backwardHeld, boolean leftHeld, boolean rightHeld) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncCelestiumWallClimbInputPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "sync_celestium_wall_climb_input"));
    public static final StreamCodec<FriendlyByteBuf, SyncCelestiumWallClimbInputPayload> CODEC = StreamCodec.ofMember(
            (payload, buf) -> {
                buf.writeBoolean(payload.sneakHeld());
                buf.writeBoolean(payload.forwardHeld());
                buf.writeBoolean(payload.backwardHeld());
                buf.writeBoolean(payload.leftHeld());
                buf.writeBoolean(payload.rightHeld());
            },
            buf -> new SyncCelestiumWallClimbInputPayload(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean())
    );

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() ->
                        CelestiumBootsManager.setWallClimbInput(
                                context.player(),
                                payload.sneakHeld(),
                                payload.forwardHeld(),
                                payload.backwardHeld(),
                                payload.leftHeld(),
                                payload.rightHeld()))
        );
    }

    public static void send(boolean sneakHeld, boolean forwardHeld, boolean backwardHeld, boolean leftHeld, boolean rightHeld) {
        ClientPlayNetworking.send(new SyncCelestiumWallClimbInputPayload(sneakHeld, forwardHeld, backwardHeld, leftHeld, rightHeld));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
