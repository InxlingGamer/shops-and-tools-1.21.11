package net.inklinggamer.celestium.advancement;

import net.inklinggamer.celestium.Celestium;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class ModAdvancementCriteria {
    public static final ActionPerformedCriterion ACTION_PERFORMED = Criteria.register(
            Celestium.MOD_ID + ":action_performed",
            new ActionPerformedCriterion()
    );

    private ModAdvancementCriteria() {
    }

    public static void register() {
        // Static field initialization handles registration.
    }

    public static void triggerAction(ServerPlayerEntity player, Identifier action) {
        ACTION_PERFORMED.trigger(player, action);
    }
}
