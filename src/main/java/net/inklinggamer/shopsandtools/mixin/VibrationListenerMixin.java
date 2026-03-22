package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.player.CelestiumBootsManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.Vibrations;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Vibrations.VibrationListener.class)
public abstract class VibrationListenerMixin {
    @Shadow
    @Final
    private Vibrations receiver;

    @Inject(method = "listen", at = @At("HEAD"), cancellable = true)
    private void shopsandtools$muffleCelestiumBootsMovement(ServerWorld world, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter, Vec3d pos, CallbackInfoReturnable<Boolean> cir) {
        Entity sourceEntity = emitter.sourceEntity();
        if ((this.receiver instanceof WardenEntity || this.receiver instanceof SculkSensorBlockEntity)
                && sourceEntity != null
                && CelestiumBootsManager.shouldMuffleMovementVibrations(sourceEntity, event)) {
            cir.setReturnValue(false);
        }
    }
}
