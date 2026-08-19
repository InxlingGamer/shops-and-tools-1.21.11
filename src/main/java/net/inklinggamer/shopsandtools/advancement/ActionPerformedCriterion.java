package net.inklinggamer.shopsandtools.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class ActionPerformedCriterion extends SimpleCriterionTrigger<ActionPerformedCriterion.Conditions> {
    public void trigger(ServerPlayer player, Identifier action) {
        this.trigger(player, conditions -> conditions.matches(action));
    }

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public record Conditions(Optional<ContextAwarePredicate> player, Identifier action) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Identifier.CODEC.fieldOf("action").forGetter(Conditions::action)
        ).apply(instance, Conditions::new));

        public boolean matches(Identifier action) {
            return this.action.equals(action);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}
