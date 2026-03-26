package net.inklinggamer.shopsandtools.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class ActionPerformedCriterion extends AbstractCriterion<ActionPerformedCriterion.Conditions> {
    public void trigger(ServerPlayerEntity player, Identifier action) {
        this.trigger(player, conditions -> conditions.matches(action));
    }

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public record Conditions(Optional<LootContextPredicate> player, Identifier action) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Identifier.CODEC.fieldOf("action").forGetter(Conditions::action)
        ).apply(instance, Conditions::new));

        public boolean matches(Identifier action) {
            return this.action.equals(action);
        }

        @Override
        public Optional<LootContextPredicate> player() {
            return this.player;
        }
    }
}
