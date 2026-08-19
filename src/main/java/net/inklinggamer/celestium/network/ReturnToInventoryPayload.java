package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record ReturnToInventoryPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReturnToInventoryPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "return_to_inventory"));
    public static final ReturnToInventoryPayload INSTANCE = new ReturnToInventoryPayload();
    public static final StreamCodec<FriendlyByteBuf, ReturnToInventoryPayload> CODEC = StreamCodec.unit(INSTANCE);

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> {
                    ItemStack cursorStack = context.player().containerMenu.getCarried().copy();
                    if (!cursorStack.isEmpty()) {
                        context.player().containerMenu.setCarried(ItemStack.EMPTY);
                        context.player().containerMenu.broadcastChanges();
                    }

                    context.player().doCloseContainer();

                    if (!cursorStack.isEmpty()) {
                        context.player().inventoryMenu.setCarried(cursorStack);
                        context.player().inventoryMenu.broadcastChanges();
                    }
                })
        );
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
