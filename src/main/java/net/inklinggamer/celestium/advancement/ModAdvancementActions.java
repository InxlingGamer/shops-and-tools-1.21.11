package net.inklinggamer.celestium.advancement;

import net.inklinggamer.celestium.Celestium;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class ModAdvancementActions {
    public static final Identifier PLAYER_JOINED = action("player_joined");
    public static final Identifier A_STRANGE_ENERGY = action("a_strange_energy");
    public static final Identifier FULLY_ASCENDED = action("fully_ascended");
    public static final Identifier TOUCH_GRASS = action("touch_grass");
    public static final Identifier BOUND_TO_THE_SKY = action("bound_to_the_sky");

    private ModAdvancementActions() {
    }

    public static void triggerPlayerJoined(ServerPlayer player) {
        trigger(player, PLAYER_JOINED);
    }

    public static void triggerAStrangeEnergy(ServerPlayer player) {
        trigger(player, A_STRANGE_ENERGY);
    }

    public static void triggerFullyAscended(ServerPlayer player) {
        trigger(player, FULLY_ASCENDED);
    }

    public static void triggerTouchGrass(ServerPlayer player) {
        trigger(player, TOUCH_GRASS);
    }

    public static void triggerBoundToTheSky(ServerPlayer player) {
        trigger(player, BOUND_TO_THE_SKY);
    }

    private static void trigger(ServerPlayer player, Identifier action) {
        ModAdvancementCriteria.triggerAction(player, action);
    }

    private static Identifier action(String path) {
        return Identifier.fromNamespaceAndPath(Celestium.MOD_ID, path);
    }
}
