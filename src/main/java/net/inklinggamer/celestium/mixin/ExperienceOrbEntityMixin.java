package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.player.CelestiumExperienceManager;
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
    private void celestium$applyCelestiumSwordXpBonus(PlayerEntity player, int experience) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            player.addExperience(CelestiumExperienceManager.applyHeldXpBonus(serverPlayer, experience));
            return;
        }

        player.addExperience(experience);
    }
}
