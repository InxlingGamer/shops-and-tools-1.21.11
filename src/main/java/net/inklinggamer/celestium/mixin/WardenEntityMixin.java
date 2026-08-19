package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.entity.WardenCombatManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Warden.class)
public abstract class WardenEntityMixin {
    @Unique
    private ServerPlayer celestium$maceAttacker;

    @Unique
    private ItemStack celestium$maceWeaponStack = ItemStack.EMPTY;

    @Unique
    private float celestium$initialCombinedHealth;

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void celestium$captureMaceDamageState(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.celestium$maceAttacker = null;
        this.celestium$maceWeaponStack = ItemStack.EMPTY;
        this.celestium$initialCombinedHealth = 0.0F;

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack mainHandStack = player.getMainHandItem();
        if (!mainHandStack.is(Items.MACE)) {
            return;
        }

        Warden warden = (Warden) (Object) this;
        this.celestium$maceAttacker = player;
        this.celestium$maceWeaponStack = mainHandStack;
        this.celestium$initialCombinedHealth = warden.getHealth() + warden.getAbsorptionAmount();
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void celestium$applyMaceDurabilityPenalty(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer player = this.celestium$maceAttacker;
        ItemStack weaponStack = this.celestium$maceWeaponStack;
        float initialCombinedHealth = this.celestium$initialCombinedHealth;

        this.celestium$maceAttacker = null;
        this.celestium$maceWeaponStack = ItemStack.EMPTY;
        this.celestium$initialCombinedHealth = 0.0F;

        if (!cir.getReturnValueZ() || player == null || weaponStack.isEmpty()) {
            return;
        }

        Warden warden = (Warden) (Object) this;
        float remainingCombinedHealth = Math.max(0.0F, warden.getHealth() + warden.getAbsorptionAmount());
        float dealtDamage = Math.max(0.0F, initialCombinedHealth - remainingCombinedHealth);
        if (dealtDamage > 0.0F) {
            WardenCombatManager.onResolvedMaceHit(player, weaponStack, true, dealtDamage);
        }
    }
}
