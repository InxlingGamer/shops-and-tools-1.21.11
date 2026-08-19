package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.player.CelestiumBootsManager;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VibrationSystem.Listener.class)
public abstract class VibrationListenerMixin {
    @Shadow
    @Final
    private VibrationSystem system;

    @Inject(method = "handleGameEvent", at = @At("HEAD"), cancellable = true)
    private void celestium$muffleCelestiumBootsMovement(ServerLevel world, Holder<GameEvent> event, GameEvent.Context emitter, Vec3 pos, CallbackInfoReturnable<Boolean> cir) {
        Entity sourceEntity = emitter.sourceEntity();
        if ((this.system instanceof Warden || this.system instanceof SculkSensorBlockEntity)
                && sourceEntity != null
                && CelestiumBootsManager.shouldMuffleMovementVibrations(sourceEntity, event)) {
            cir.setReturnValue(false);
        }
    }
}
