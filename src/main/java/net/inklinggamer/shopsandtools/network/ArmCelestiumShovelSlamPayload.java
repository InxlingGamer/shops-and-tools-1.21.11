package net.inklinggamer.shopsandtools.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.player.CelestiumShovelManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ArmCelestiumShovelSlamPayload() implements CustomPacketPayload {
    public static final Type<ArmCelestiumShovelSlamPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "arm_celestium_shovel_slam"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmCelestiumShovelSlamPayload> CODEC =
            StreamCodec.unit(new ArmCelestiumShovelSlamPayload());

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> CelestiumShovelManager.armSlam(context.player()))
        );
    }

    public static void send() {
        ClientPlayNetworking.send(new ArmCelestiumShovelSlamPayload());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
