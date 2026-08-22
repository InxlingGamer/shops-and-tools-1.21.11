package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record SyncCelestiumRagePayload(int stacks) implements CustomPayload {
    public static final CustomPayload.Id<SyncCelestiumRagePayload> ID =
            new CustomPayload.Id<>(Identifier.of(Celestium.MOD_ID, "sync_celestium_rage"));
    public static final PacketCodec<PacketByteBuf, SyncCelestiumRagePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            SyncCelestiumRagePayload::stacks,
            SyncCelestiumRagePayload::new
    );

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }

    public static void send(ServerPlayerEntity player, int stacks) {
        ServerPlayNetworking.send(player, new SyncCelestiumRagePayload(stacks));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
