package net.inklinggamer.celestium.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(PlayerAdvancements.class)
public interface PlayerAdvancementsAccessor {
    @Accessor("progressChanged")
    Set<AdvancementHolder> celestium$getProgressChanged();
}
