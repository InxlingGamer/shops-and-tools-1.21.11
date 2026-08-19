package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.player.CelestiumPickaxeManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.HitResult;

public record ToggleCelestiumPickaxeAreaModePayload() implements CustomPacketPayload {
    public static final Type<ToggleCelestiumPickaxeAreaModePayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "toggle_celestium_pickaxe_area_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCelestiumPickaxeAreaModePayload> CODEC =
            StreamCodec.unit(new ToggleCelestiumPickaxeAreaModePayload());

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> {
                    HitResult hitResult = CelestiumPickaxeManager.getCurrentTarget(context.player());
                    if (!CelestiumPickaxeManager.canToggleAreaMining(context.player(), hitResult)) {
                        return;
                    }

                    boolean enabled = CelestiumPickaxeManager.toggleAreaMining(context.player());
                    context.player().sendOverlayMessage(Component.translatable(
                            enabled
                                    ? "message.celestium.celestium_pickaxe_area_enabled"
                                    : "message.celestium.celestium_pickaxe_area_disabled"
                    ));
                })
        );
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
