package net.inklinggamer.celestium.client;

import net.inklinggamer.celestium.item.CelestiumSpearHelper;
import net.inklinggamer.celestium.network.AttackWithCelestiumSpearPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public final class CelestiumSpearClient {
    private CelestiumSpearClient() {
    }

    public static boolean handleExtendedAttack(MinecraftClient client) {
        if (client.player == null
                || client.world == null
                || client.targetedEntity != null
                || !CelestiumSpearHelper.isCelestiumSpearEquipped(client.player)
                || client.player.getAttackCooldownProgress(0.5F) < 0.99F) {
            return false;
        }

        Vec3d start = client.player.getEyePos();
        Vec3d reachVector = client.player.getRotationVector().multiply(AttackWithCelestiumSpearPayload.ATTACK_RANGE);
        Vec3d end = start.add(reachVector);
        EntityHitResult hit = ProjectileUtil.raycast(
                client.player,
                start,
                end,
                client.player.getBoundingBox().stretch(reachVector).expand(1.0D),
                entity -> !entity.isSpectator() && entity.canHit(),
                AttackWithCelestiumSpearPayload.ATTACK_RANGE * AttackWithCelestiumSpearPayload.ATTACK_RANGE
        );
        if (hit == null) {
            return false;
        }

        if (client.crosshairTarget != null
                && client.crosshairTarget.getType() == HitResult.Type.BLOCK
                && start.squaredDistanceTo(client.crosshairTarget.getPos()) < start.squaredDistanceTo(hit.getPos())) {
            return false;
        }

        CelestiumClientNetworking.send(new AttackWithCelestiumSpearPayload(hit.getEntity().getId()));
        client.player.swingHand(Hand.MAIN_HAND);
        client.player.resetLastAttackedTicks();
        return true;
    }
}
