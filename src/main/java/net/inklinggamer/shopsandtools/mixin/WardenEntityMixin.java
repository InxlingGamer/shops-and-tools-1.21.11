package net.inklinggamer.shopsandtools.mixin;

import net.inklinggamer.shopsandtools.entity.WardenCombatManager;
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
    private ServerPlayer shopsandtools$maceAttacker;

    @Unique
    private ItemStack shopsandtools$maceWeaponStack = ItemStack.EMPTY;

    @Unique
    private float shopsandtools$initialCombinedHealth;

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void shopsandtools$captureMaceDamageState(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.shopsandtools$maceAttacker = null;
        this.shopsandtools$maceWeaponStack = ItemStack.EMPTY;
        this.shopsandtools$initialCombinedHealth = 0.0F;

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack mainHandStack = player.getMainHandItem();
        if (!mainHandStack.is(Items.MACE)) {
            return;
        }

        Warden warden = (Warden) (Object) this;
        this.shopsandtools$maceAttacker = player;
        this.shopsandtools$maceWeaponStack = mainHandStack;
        this.shopsandtools$initialCombinedHealth = warden.getHealth() + warden.getAbsorptionAmount();
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void shopsandtools$applyMaceDurabilityPenalty(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer player = this.shopsandtools$maceAttacker;
        ItemStack weaponStack = this.shopsandtools$maceWeaponStack;
        float initialCombinedHealth = this.shopsandtools$initialCombinedHealth;

        this.shopsandtools$maceAttacker = null;
        this.shopsandtools$maceWeaponStack = ItemStack.EMPTY;
        this.shopsandtools$initialCombinedHealth = 0.0F;

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
