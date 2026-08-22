package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.item.CelestiumSpearHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public record AttackWithCelestiumSpearPayload(int entityId) implements CustomPayload {
    public static final double ATTACK_RANGE = 4.5D;

    public static final Id<AttackWithCelestiumSpearPayload> ID =
            new Id<>(Identifier.of(Celestium.MOD_ID, "attack_with_celestium_spear"));
    public static final PacketCodec<PacketByteBuf, AttackWithCelestiumSpearPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            AttackWithCelestiumSpearPayload::entityId,
            AttackWithCelestiumSpearPayload::new
    );

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> attackIfValid(context.player(), payload.entityId()))
        );
    }

    private static void attackIfValid(ServerPlayerEntity player, int entityId) {
        if (!CelestiumSpearHelper.isCelestiumSpearEquipped(player)
                || player.getAttackCooldownProgress(0.5F) < 0.99F) {
            return;
        }

        Entity target = player.getServerWorld().getEntityById(entityId);
        if (!(target instanceof LivingEntity livingTarget)
                || target == player
                || !target.isAlive()
                || target.isSpectator()
                || !player.canSee(livingTarget)
                || target.getBoundingBox().expand(0.5D).squaredMagnitude(player.getEyePos()) > ATTACK_RANGE * ATTACK_RANGE) {
            return;
        }

        player.attack(target);
        player.swingHand(Hand.MAIN_HAND);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
