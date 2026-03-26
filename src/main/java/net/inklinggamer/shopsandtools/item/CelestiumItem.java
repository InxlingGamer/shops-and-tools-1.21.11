package net.inklinggamer.shopsandtools.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.joml.Vector3f;

public class CelestiumItem extends Item {
    public CelestiumItem(Settings settings) {
        super(settings);
    }

    public int getLightEmission(PlayerEntity player, ItemStack stack) {
        return CelestiumHeldLight.getPearlescentFroglightLightEmission();
    }

    public Vector3f getLightColor(PlayerEntity player, ItemStack stack) {
        return CelestiumHeldLight.createPearlescentFroglightColor();
    }
}

final class CelestiumHeldLight {
    private static final int PEARLESCENT_FROGLIGHT_LIGHT_EMISSION = 15;
    private static final float PEARLESCENT_FROGLIGHT_RED = 1.1F;
    private static final float PEARLESCENT_FROGLIGHT_GREEN = 0.5F;
    private static final float PEARLESCENT_FROGLIGHT_BLUE = 0.9F;

    private CelestiumHeldLight() {
    }

    static int getPearlescentFroglightLightEmission() {
        return PEARLESCENT_FROGLIGHT_LIGHT_EMISSION;
    }

    static Vector3f createPearlescentFroglightColor() {
        return new Vector3f(
                PEARLESCENT_FROGLIGHT_RED,
                PEARLESCENT_FROGLIGHT_GREEN,
                PEARLESCENT_FROGLIGHT_BLUE
        );
    }
}
