package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReturnToInventoryPayload() implements CustomPayload {
    public static final CustomPayload.Id<ReturnToInventoryPayload> ID =
            new CustomPayload.Id<>(Identifier.of(Celestium.MOD_ID, "return_to_inventory"));
    public static final ReturnToInventoryPayload INSTANCE = new ReturnToInventoryPayload();
    public static final PacketCodec<PacketByteBuf, ReturnToInventoryPayload> CODEC = PacketCodec.unit(INSTANCE);

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> {
                    ItemStack cursorStack = context.player().currentScreenHandler.getCursorStack().copy();
                    if (!cursorStack.isEmpty()) {
                        context.player().currentScreenHandler.setCursorStack(ItemStack.EMPTY);
                        context.player().currentScreenHandler.sendContentUpdates();
                    }

                    context.player().onHandledScreenClosed();

                    if (!cursorStack.isEmpty()) {
                        context.player().playerScreenHandler.setCursorStack(cursorStack);
                        context.player().playerScreenHandler.sendContentUpdates();
                    }
                })
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
