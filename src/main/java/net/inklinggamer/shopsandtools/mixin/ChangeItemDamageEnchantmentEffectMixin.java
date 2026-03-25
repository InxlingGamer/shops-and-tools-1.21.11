package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.item.CelestiumSpearHelper;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.entity.ChangeItemDamageEnchantmentEffect;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChangeItemDamageEnchantmentEffect.class)
public abstract class ChangeItemDamageEnchantmentEffectMixin {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void shopsandtools$preventCelestiumSpearLungeDamage(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos, CallbackInfo ci) {
        if (CelestiumSpearHelper.isCelestiumSpear(context.stack())) {
            ci.cancel();
        }
    }
}
