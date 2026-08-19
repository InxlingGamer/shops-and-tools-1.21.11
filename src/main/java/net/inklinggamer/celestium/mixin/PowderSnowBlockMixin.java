package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.player.CelestiumBootsManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.level.block.PowderSnowBlock")
public abstract class PowderSnowBlockMixin {
    @Inject(method = "canEntityWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
    private static void celestium$allowCelestiumBootsToWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && CelestiumBootsManager.isCelestiumBootsEquipped(player)) {
            cir.setReturnValue(true);
        }
    }
}
