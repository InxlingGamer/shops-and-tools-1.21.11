package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.player.CelestiumLeggingsManager;
import net.inklinggamer.celestium.screen.CelestiumPortableCraftingScreenHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenCelestiumCraftingPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenCelestiumCraftingPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "open_celestium_crafting"));
    public static final OpenCelestiumCraftingPayload INSTANCE = new OpenCelestiumCraftingPayload();
    public static final StreamCodec<FriendlyByteBuf, OpenCelestiumCraftingPayload> CODEC = StreamCodec.unit(INSTANCE);

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ID, CODEC);
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
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
