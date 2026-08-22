package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.world.CelestiumWorldVersionGuard;
import net.minecraft.SharedConstants;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(LevelStorage.class)
public abstract class LevelStorageMixin {
    @Shadow
    @Final
    private Path savesDirectory;

    @Inject(method = {"createSession", "createSessionWithoutSymlinkCheck"}, at = @At("HEAD"))
    private void celestium$rejectNewerWorld(
            String directoryName,
            CallbackInfoReturnable<LevelStorage.Session> cir
    ) throws IOException {
        int supportedDataVersion = SharedConstants.getGameVersion().getSaveVersion().getId();
        CelestiumWorldVersionGuard.rejectNewerWorld(this.savesDirectory.resolve(directoryName), supportedDataVersion);
    }
}
