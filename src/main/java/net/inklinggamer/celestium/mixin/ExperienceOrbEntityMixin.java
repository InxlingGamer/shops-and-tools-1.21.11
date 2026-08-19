package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.player.CelestiumExperienceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEntityMixin {
    @Redirect(
            method = "playerTouch",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;giveExperiencePoints(I)V"
            )
    )
    private void celestium$applyCelestiumSwordXpBonus(Player player, int experience) {
        if (player instanceof ServerPlayer serverPlayer) {
            player.giveExperiencePoints(CelestiumExperienceManager.applyHeldXpBonus(serverPlayer, experience));
            return;
        }

        player.giveExperiencePoints(experience);
    }
}
