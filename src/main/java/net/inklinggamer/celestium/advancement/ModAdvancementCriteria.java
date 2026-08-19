package net.inklinggamer.celestium.advancement;

import net.inklinggamer.celestium.Celestium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class ModAdvancementCriteria {
    public static final ActionPerformedCriterion ACTION_PERFORMED = Registry.register(
            BuiltInRegistries.TRIGGER_TYPES,
            Identifier.fromNamespaceAndPath(Celestium.MOD_ID, "action_performed"),
            new ActionPerformedCriterion()
    );

    private ModAdvancementCriteria() {
    }

    public static void register() {
        // Static field initialization handles registration.
    }

    public static void triggerAction(ServerPlayer player, Identifier action) {
        ACTION_PERFORMED.trigger(player, action);
    }
}
