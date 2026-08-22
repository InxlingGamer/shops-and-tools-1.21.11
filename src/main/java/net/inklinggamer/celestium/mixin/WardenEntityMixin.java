package net.inklinggamer.celestium.mixin;

import net.inklinggamer.celestium.entity.WardenCombatManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WardenEntity.class)
public abstract class WardenEntityMixin {
    @Unique
    private ServerPlayerEntity celestium$maceAttacker;

    @Unique
    private ItemStack celestium$maceWeaponStack = ItemStack.EMPTY;

    @Unique
    private float celestium$initialCombinedHealth;

    @Inject(method = "damage", at = @At("HEAD"))
    private void celestium$captureMaceDamageState(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.celestium$maceAttacker = null;
        this.celestium$maceWeaponStack = ItemStack.EMPTY;
        this.celestium$initialCombinedHealth = 0.0F;

        if (!(source.getAttacker() instanceof ServerPlayerEntity player)) {
            return;
        }

        ItemStack mainHandStack = player.getMainHandStack();
        if (!mainHandStack.isOf(Items.MACE)) {
            return;
        }

        WardenEntity warden = (WardenEntity) (Object) this;
        this.celestium$maceAttacker = player;
        this.celestium$maceWeaponStack = mainHandStack;
        this.celestium$initialCombinedHealth = warden.getHealth() + warden.getAbsorptionAmount();
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void celestium$applyMaceDurabilityPenalty(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = this.celestium$maceAttacker;
        ItemStack weaponStack = this.celestium$maceWeaponStack;
        float initialCombinedHealth = this.celestium$initialCombinedHealth;

        this.celestium$maceAttacker = null;
        this.celestium$maceWeaponStack = ItemStack.EMPTY;
        this.celestium$initialCombinedHealth = 0.0F;

        if (!cir.getReturnValueZ() || player == null || weaponStack.isEmpty()) {
            return;
        }

        WardenEntity warden = (WardenEntity) (Object) this;
        float remainingCombinedHealth = Math.max(0.0F, warden.getHealth() + warden.getAbsorptionAmount());
        float dealtDamage = Math.max(0.0F, initialCombinedHealth - remainingCombinedHealth);
        if (dealtDamage > 0.0F) {
            WardenCombatManager.onResolvedMaceHit(player, weaponStack, true, dealtDamage);
        }
    }
}
