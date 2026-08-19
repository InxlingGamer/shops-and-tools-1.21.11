package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.player.CelestiumHorseArmorManager;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void celestium$tickCelestiumHorseArmor(CallbackInfo ci) {
        CelestiumHorseArmorManager.tickHorse((AbstractHorse) (Object) this);
    }
}
