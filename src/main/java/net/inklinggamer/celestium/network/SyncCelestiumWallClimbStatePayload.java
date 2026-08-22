package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public record SyncCelestiumWallClimbStatePayload(
        boolean active,
        int wallDirectionId,
        double velocityX,
        double velocityY,
        double velocityZ
) implements CustomPayload {
    public static final CustomPayload.Id<SyncCelestiumWallClimbStatePayload> ID =
            new CustomPayload.Id<>(Identifier.of(Celestium.MOD_ID, "sync_celestium_wall_climb_state"));
    public static final PacketCodec<PacketByteBuf, SyncCelestiumWallClimbStatePayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeBoolean(payload.active());
                buf.writeInt(payload.wallDirectionId());
                buf.writeDouble(payload.velocityX());
                buf.writeDouble(payload.velocityY());
                buf.writeDouble(payload.velocityZ());
            },
            buf -> new SyncCelestiumWallClimbStatePayload(
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble()
            )
    );

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }

    public static void send(ServerPlayerEntity player, boolean active, Direction wallDirection, Vec3d velocity) {
        ServerPlayNetworking.send(player, new SyncCelestiumWallClimbStatePayload(
                active,
                wallDirection == null ? -1 : wallDirection.ordinal(),
                velocity.x,
                velocity.y,
                velocity.z
        ));
    }

    public Direction wallDirection() {
        Direction[] directions = Direction.values();
        return this.wallDirectionId < 0 || this.wallDirectionId >= directions.length ? null : directions[this.wallDirectionId];
    }

    public Vec3d velocity() {
        return new Vec3d(this.velocityX, this.velocityY, this.velocityZ);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
