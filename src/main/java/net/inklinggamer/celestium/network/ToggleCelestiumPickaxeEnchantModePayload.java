package net.inklinggamer.celestium.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.celestium.Celestium;
import net.inklinggamer.celestium.item.CelestiumPickaxeHelper;
import net.inklinggamer.celestium.player.CelestiumPickaxeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public record ToggleCelestiumPickaxeEnchantModePayload(int slotId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleCelestiumPickaxeEnchantModePayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "toggle_celestium_pickaxe_enchant_mode"));
    public static final StreamCodec<FriendlyByteBuf, ToggleCelestiumPickaxeEnchantModePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ToggleCelestiumPickaxeEnchantModePayload::slotId,
            ToggleCelestiumPickaxeEnchantModePayload::new
    );

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> {
                    AbstractContainerMenu handler = context.player().containerMenu;
                    if (handler != context.player().inventoryMenu || payload.slotId() < 0 || payload.slotId() >= handler.slots.size()) {
                        return;
                    }

                    Slot slot = handler.getSlot(payload.slotId());
                    if (!slot.hasItem() || !CelestiumPickaxeHelper.isCelestiumPickaxe(slot.getItem())) {
                        return;
                    }

                    boolean silkModeEnabled = CelestiumPickaxeManager.toggleEnchantMode(slot.getItem(), context.player());
                    slot.setChanged();
                    handler.broadcastChanges();
                    context.player().sendOverlayMessage(net.minecraft.network.chat.Component.translatable(
                            silkModeEnabled
                                    ? "message.celestium.celestium_pickaxe_mode_silk_touch"
                                    : "message.celestium.celestium_pickaxe_mode_fortune"
                    ));
                })
        );
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
