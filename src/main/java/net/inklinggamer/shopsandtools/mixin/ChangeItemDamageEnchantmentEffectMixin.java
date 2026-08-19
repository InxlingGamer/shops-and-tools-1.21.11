package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.item.CelestiumSpearHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.ChangeItemDamage;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChangeItemDamage.class)
public abstract class ChangeItemDamageEnchantmentEffectMixin {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void shopsandtools$preventCelestiumSpearLungeDamage(ServerLevel world, int level, EnchantedItemInUse context, Entity user, Vec3 pos, CallbackInfo ci) {
        if (CelestiumSpearHelper.isCelestiumSpear(context.itemStack())) {
            ci.cancel();
        }
    }
}
