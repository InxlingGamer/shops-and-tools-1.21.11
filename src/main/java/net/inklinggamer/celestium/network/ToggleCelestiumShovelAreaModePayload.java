package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.player.CelestiumShovelManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;

public record ToggleCelestiumShovelAreaModePayload() implements CustomPayload {
    public static final Id<ToggleCelestiumShovelAreaModePayload> ID =
            new Id<>(Identifier.of(Celestium.MOD_ID, "toggle_celestium_shovel_area_mode"));
    public static final PacketCodec<RegistryByteBuf, ToggleCelestiumShovelAreaModePayload> CODEC =
            PacketCodec.unit(new ToggleCelestiumShovelAreaModePayload());

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> {
                    HitResult hitResult = CelestiumShovelManager.getCurrentTarget(context.player());
                    if (!CelestiumShovelManager.canToggleAreaMining(context.player(), hitResult)) {
                        return;
                    }

                    boolean enabled = CelestiumShovelManager.toggleAreaMining(context.player());
                    context.player().sendMessage(Text.translatable(
                            enabled
                                    ? "message.celestium.celestium_shovel_area_enabled"
                                    : "message.celestium.celestium_shovel_area_disabled"
                    ), true);
                })
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
