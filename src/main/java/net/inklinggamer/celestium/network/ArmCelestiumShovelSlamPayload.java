package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.player.CelestiumShovelManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ArmCelestiumShovelSlamPayload() implements CustomPacketPayload {
    public static final Type<ArmCelestiumShovelSlamPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "arm_celestium_shovel_slam"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmCelestiumShovelSlamPayload> CODEC =
            StreamCodec.unit(new ArmCelestiumShovelSlamPayload());

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> CelestiumShovelManager.armSlam(context.player()))
        );
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
