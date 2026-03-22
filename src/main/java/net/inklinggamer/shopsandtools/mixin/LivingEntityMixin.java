package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.player.CelestiumLeggingsManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "damage", at = @At("RETURN"))
    private void shopsandtools$applyCelestiumRetaliation(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            CelestiumLeggingsManager.onPlayerDamaged((LivingEntity) (Object) this, source);
        }
    }
}
