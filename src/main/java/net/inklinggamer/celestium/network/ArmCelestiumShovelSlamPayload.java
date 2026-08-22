package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.player.CelestiumShovelManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ArmCelestiumShovelSlamPayload() implements CustomPayload {
    public static final Id<ArmCelestiumShovelSlamPayload> ID =
            new Id<>(Identifier.of(Celestium.MOD_ID, "arm_celestium_shovel_slam"));
    public static final PacketCodec<RegistryByteBuf, ArmCelestiumShovelSlamPayload> CODEC =
            PacketCodec.unit(new ArmCelestiumShovelSlamPayload());

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> CelestiumShovelManager.armSlam(context.player()))
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
