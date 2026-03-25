package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.player.CelestiumSpearManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
    @Inject(method = "tickNewAi", at = @At("HEAD"), cancellable = true)
    private void shopsandtools$cancelAiWhileStunned(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;
        if (!CelestiumSpearManager.isStunned(mob)) {
            return;
        }

        mob.getNavigation().stop();
        mob.setVelocity(Vec3d.ZERO);
        ci.cancel();
    }

    @Inject(method = "tryAttack", at = @At("HEAD"), cancellable = true)
    private void shopsandtools$preventAttackWhileStunned(ServerWorld world, Entity target, CallbackInfoReturnable<Boolean> cir) {
        MobEntity mob = (MobEntity) (Object) this;
        if (CelestiumSpearManager.isStunned(mob)) {
            mob.getNavigation().stop();
            mob.setVelocity(Vec3d.ZERO);
            cir.setReturnValue(false);
        }
    }
}
