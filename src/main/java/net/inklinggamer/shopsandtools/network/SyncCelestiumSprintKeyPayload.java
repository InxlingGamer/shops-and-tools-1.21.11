package net.inklinggamer.shopsandtools.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.player.CelestiumBootsManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncCelestiumSprintKeyPayload(boolean held) implements CustomPayload {
    public static final CustomPayload.Id<SyncCelestiumSprintKeyPayload> ID =
            new CustomPayload.Id<>(Identifier.of(ShopsAndTools.MOD_ID, "sync_celestium_sprint_key"));
    public static final PacketCodec<PacketByteBuf, SyncCelestiumSprintKeyPayload> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeBoolean(payload.held()),
            buf -> new SyncCelestiumSprintKeyPayload(buf.readBoolean())
    );

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> CelestiumBootsManager.setSprintKeyHeld(context.player(), payload.held()))
        );
    }

    public static void send(boolean held) {
        ClientPlayNetworking.send(new SyncCelestiumSprintKeyPayload(held));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
