package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.player.CelestiumSpearManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobEntityMixin {
    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void shopsandtools$cancelAiWhileStunned(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!CelestiumSpearManager.isStunned(mob)) {
            return;
        }

        mob.getNavigation().stop();
        mob.setDeltaMovement(Vec3.ZERO);
        ci.cancel();
    }

    @Inject(method = "doHurtTarget", at = @At("HEAD"), cancellable = true)
    private void shopsandtools$preventAttackWhileStunned(ServerLevel world, Entity target, CallbackInfoReturnable<Boolean> cir) {
        Mob mob = (Mob) (Object) this;
        if (CelestiumSpearManager.isStunned(mob)) {
            mob.getNavigation().stop();
            mob.setDeltaMovement(Vec3.ZERO);
            cir.setReturnValue(false);
        }
    }
}
