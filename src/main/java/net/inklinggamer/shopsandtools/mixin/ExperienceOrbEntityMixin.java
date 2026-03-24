package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.player.CelestiumPickaxeManager;
import net.inklinggamer.shopsandtools.player.CelestiumSwordManager;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin {
    @Redirect(
            method = "onPlayerCollision",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;addExperience(I)V"
            )
    )
    private void shopsandtools$applyCelestiumSwordXpBonus(PlayerEntity player, int experience) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            int adjustedExperience = CelestiumSwordManager.applyXpBonus(serverPlayer, experience);
            adjustedExperience = CelestiumPickaxeManager.applyXpBonus(serverPlayer, adjustedExperience);
            player.addExperience(adjustedExperience);
            return;
        }

        player.addExperience(experience);
    }
}
