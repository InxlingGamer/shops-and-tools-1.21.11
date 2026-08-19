package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.player.CelestiumShovelManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.HitResult;

public record ToggleCelestiumShovelAreaModePayload() implements CustomPacketPayload {
    public static final Type<ToggleCelestiumShovelAreaModePayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "toggle_celestium_shovel_area_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCelestiumShovelAreaModePayload> CODEC =
            StreamCodec.unit(new ToggleCelestiumShovelAreaModePayload());

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> {
                    HitResult hitResult = CelestiumShovelManager.getCurrentTarget(context.player());
                    if (!CelestiumShovelManager.canToggleAreaMining(context.player(), hitResult)) {
                        return;
                    }

                    boolean enabled = CelestiumShovelManager.toggleAreaMining(context.player());
                    context.player().sendOverlayMessage(Component.translatable(
                            enabled
                                    ? "message.celestium.celestium_shovel_area_enabled"
                                    : "message.celestium.celestium_shovel_area_disabled"
                    ));
                })
        );
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
