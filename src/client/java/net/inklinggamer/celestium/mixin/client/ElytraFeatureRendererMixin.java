package net.inklinggamer.celestium.mixin.client;

import net.inklinggamer.celestium.item.ModItems;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ElytraFeatureRenderer.class)
public abstract class ElytraFeatureRendererMixin {
    @Redirect(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private boolean celestium$renderCelestiumElytra(ItemStack stack, Item expectedItem) {
        return stack.isOf(expectedItem)
                || (expectedItem == Items.ELYTRA && stack.isOf(ModItems.CELESTIUM_ELYTRA_CHESTPLATE));
    }
}
