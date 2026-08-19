package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public record SyncCelestiumWallClimbStatePayload(
        boolean active,
        int wallDirectionId,
        double velocityX,
        double velocityY,
        double velocityZ
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncCelestiumWallClimbStatePayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "sync_celestium_wall_climb_state"));
    public static final StreamCodec<FriendlyByteBuf, SyncCelestiumWallClimbStatePayload> CODEC = StreamCodec.ofMember(
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
        PayloadTypeRegistry.clientboundPlay().register(ID, CODEC);
    }


    public static void send(ServerPlayer player, boolean active, Direction wallDirection, Vec3 velocity) {
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

    public Vec3 velocity() {
        return new Vec3(this.velocityX, this.velocityY, this.velocityZ);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
