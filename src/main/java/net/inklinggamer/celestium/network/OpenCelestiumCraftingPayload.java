package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.player.CelestiumLeggingsManager;
import net.inklinggamer.celestium.screen.CelestiumPortableCraftingScreenHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenCelestiumCraftingPayload() implements CustomPayload {
    public static final CustomPayload.Id<OpenCelestiumCraftingPayload> ID =
            new CustomPayload.Id<>(Identifier.of(Celestium.MOD_ID, "open_celestium_crafting"));
    public static final OpenCelestiumCraftingPayload INSTANCE = new OpenCelestiumCraftingPayload();
    public static final PacketCodec<PacketByteBuf, OpenCelestiumCraftingPayload> CODEC = PacketCodec.unit(INSTANCE);

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> {
                    if (!CelestiumLeggingsManager.isCelestiumLeggingsEquipped(context.player())) {
                        return;
                    }

                    CelestiumPortableCraftingScreenHandler.openFor(context.player());
                })
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
