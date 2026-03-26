package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.entity.WardenCombatManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WardenEntity.class)
public abstract class WardenEntityMixin {
    @Unique
    private ServerPlayerEntity shopsandtools$maceAttacker;

    @Unique
    private ItemStack shopsandtools$maceWeaponStack = ItemStack.EMPTY;

    @Unique
    private float shopsandtools$initialCombinedHealth;

    @Inject(method = "damage", at = @At("HEAD"))
    private void shopsandtools$captureMaceDamageState(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.shopsandtools$maceAttacker = null;
        this.shopsandtools$maceWeaponStack = ItemStack.EMPTY;
        this.shopsandtools$initialCombinedHealth = 0.0F;

        if (!(source.getAttacker() instanceof ServerPlayerEntity player)) {
            return;
        }

        ItemStack mainHandStack = player.getMainHandStack();
        if (!mainHandStack.isOf(Items.MACE)) {
            return;
        }

        WardenEntity warden = (WardenEntity) (Object) this;
        this.shopsandtools$maceAttacker = player;
        this.shopsandtools$maceWeaponStack = mainHandStack;
        this.shopsandtools$initialCombinedHealth = warden.getHealth() + warden.getAbsorptionAmount();
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void shopsandtools$applyMaceDurabilityPenalty(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = this.shopsandtools$maceAttacker;
        ItemStack weaponStack = this.shopsandtools$maceWeaponStack;
        float initialCombinedHealth = this.shopsandtools$initialCombinedHealth;

        this.shopsandtools$maceAttacker = null;
        this.shopsandtools$maceWeaponStack = ItemStack.EMPTY;
        this.shopsandtools$initialCombinedHealth = 0.0F;

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
