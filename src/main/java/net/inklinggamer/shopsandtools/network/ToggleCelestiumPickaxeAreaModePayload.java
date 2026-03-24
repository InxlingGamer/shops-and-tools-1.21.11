package net.inklinggamer.shopsandtools.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.player.CelestiumPickaxeManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;

public record ToggleCelestiumPickaxeAreaModePayload() implements CustomPayload {
    public static final Id<ToggleCelestiumPickaxeAreaModePayload> ID =
            new Id<>(Identifier.of(ShopsAndTools.MOD_ID, "toggle_celestium_pickaxe_area_mode"));
    public static final PacketCodec<RegistryByteBuf, ToggleCelestiumPickaxeAreaModePayload> CODEC =
            PacketCodec.unit(new ToggleCelestiumPickaxeAreaModePayload());

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> {
                    HitResult hitResult = CelestiumPickaxeManager.getCurrentTarget(context.player());
                    if (!CelestiumPickaxeManager.canToggleAreaMining(context.player(), hitResult)) {
                        return;
                    }

                    boolean enabled = CelestiumPickaxeManager.toggleAreaMining(context.player());
                    context.player().sendMessage(Text.translatable(
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
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
