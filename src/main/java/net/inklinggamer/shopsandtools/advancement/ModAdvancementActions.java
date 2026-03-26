package net.inklinggamer.shopsandtools.advancement;

import net.inklinggamer.shopsandtools.ShopsAndTools;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class ModAdvancementActions {
    public static final Identifier PLAYER_JOINED = action("player_joined");
    public static final Identifier A_STRANGE_ENERGY = action("a_strange_energy");
    public static final Identifier FULLY_ASCENDED = action("fully_ascended");
    public static final Identifier TOUCH_GRASS = action("touch_grass");
    public static final Identifier BOUND_TO_THE_SKY = action("bound_to_the_sky");

    private ModAdvancementActions() {
    }

    public static void triggerPlayerJoined(ServerPlayerEntity player) {
        trigger(player, PLAYER_JOINED);
    }

    public static void triggerAStrangeEnergy(ServerPlayerEntity player) {
        trigger(player, A_STRANGE_ENERGY);
    }

    public static void triggerFullyAscended(ServerPlayerEntity player) {
        trigger(player, FULLY_ASCENDED);
    }

    public static void triggerTouchGrass(ServerPlayerEntity player) {
        trigger(player, TOUCH_GRASS);
    }

    public static void triggerBoundToTheSky(ServerPlayerEntity player) {
        trigger(player, BOUND_TO_THE_SKY);
    }

    private static void trigger(ServerPlayerEntity player, Identifier action) {
        ModAdvancementCriteria.triggerAction(player, action);
    }

    private static Identifier action(String path) {
        return Identifier.of(ShopsAndTools.MOD_ID, path);
    }
}
