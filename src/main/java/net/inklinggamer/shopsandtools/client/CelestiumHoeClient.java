package net.inklinggamer.shopsandtools.client;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.inklinggamer.shopsandtools.item.CelestiumHoeHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class CelestiumHoeClient {
    private static final List<BlockPos> outlinePositions = new ArrayList<>();

    private CelestiumHoeClient() {
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            outlinePositions.clear();
            return;
        }

        updateOutline(client);
    }

    public static void render(WorldRenderContext context) {
        CelestiumPickaxeOutlineRenderer.render(context, outlinePositions);
    }

    private static void updateOutline(MinecraftClient client) {
        outlinePositions.clear();
        if (client.player == null || client.world == null || !CelestiumHoeHelper.isCelestiumHoe(client.player.getMainHandStack())) {
            return;
        }

        if (!(client.crosshairTarget instanceof BlockHitResult hitResult)) {
            return;
        }

        outlinePositions.addAll(CelestiumHoeHelper.getHarvestTargets(hitResult.getBlockPos(), client.world::getBlockState));
    }
}
