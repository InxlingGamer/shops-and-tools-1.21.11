package net.inklinggamer.shopsandtools.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.player.CelestiumPickaxeManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.HitResult;

public record ToggleCelestiumPickaxeAreaModePayload() implements CustomPacketPayload {
    public static final Type<ToggleCelestiumPickaxeAreaModePayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(ShopsAndTools.MOD_ID, "toggle_celestium_pickaxe_area_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCelestiumPickaxeAreaModePayload> CODEC =
            StreamCodec.unit(new ToggleCelestiumPickaxeAreaModePayload());

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> {
                    HitResult hitResult = CelestiumPickaxeManager.getCurrentTarget(context.player());
                    if (!CelestiumPickaxeManager.canToggleAreaMining(context.player(), hitResult)) {
                        return;
                    }

                    boolean enabled = CelestiumPickaxeManager.toggleAreaMining(context.player());
                    context.player().displayClientMessage(Component.translatable(
                            enabled
                                    ? "message.shopsandtools.celestium_pickaxe_area_enabled"
                                    : "message.shopsandtools.celestium_pickaxe_area_disabled"
                    ), true);
                })
        );
    }

    public static void send() {
        ClientPlayNetworking.send(new ToggleCelestiumPickaxeAreaModePayload());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
