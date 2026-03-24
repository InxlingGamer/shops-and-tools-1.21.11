package net.inklinggamer.shopsandtools.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.inklinggamer.shopsandtools.item.CelestiumPickaxeHelper;
import net.inklinggamer.shopsandtools.player.CelestiumPickaxeManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public record ToggleCelestiumPickaxeEnchantModePayload(int slotId) implements CustomPayload {
    public static final CustomPayload.Id<ToggleCelestiumPickaxeEnchantModePayload> ID =
            new CustomPayload.Id<>(Identifier.of(ShopsAndTools.MOD_ID, "toggle_celestium_pickaxe_enchant_mode"));
    public static final PacketCodec<PacketByteBuf, ToggleCelestiumPickaxeEnchantModePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            ToggleCelestiumPickaxeEnchantModePayload::slotId,
            ToggleCelestiumPickaxeEnchantModePayload::new
    );

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
                context.server().execute(() -> {
                    ScreenHandler handler = context.player().currentScreenHandler;
                    if (handler != context.player().playerScreenHandler || payload.slotId() < 0 || payload.slotId() >= handler.slots.size()) {
                        return;
                    }

                    Slot slot = handler.getSlot(payload.slotId());
                    if (!slot.hasStack() || !CelestiumPickaxeHelper.isCelestiumPickaxe(slot.getStack())) {
                        return;
                    }

                    boolean silkModeEnabled = CelestiumPickaxeManager.toggleEnchantMode(slot.getStack(), context.player());
                    slot.markDirty();
                    handler.sendContentUpdates();
                    context.player().sendMessage(net.minecraft.text.Text.translatable(
                            silkModeEnabled
                                    ? "message.shopsandtools.celestium_pickaxe_mode_silk_touch"
                                    : "message.shopsandtools.celestium_pickaxe_mode_fortune"
                    ), true);
                })
        );
    }

    public static void send(int slotId) {
        ClientPlayNetworking.send(new ToggleCelestiumPickaxeEnchantModePayload(slotId));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
