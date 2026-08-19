package net.inklinggamer.celestium.client;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.inklinggamer.celestium.item.CelestiumHoeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import java.util.ArrayList;
import java.util.List;

public final class CelestiumHoeClient {
    private static final List<BlockPos> outlinePositions = new ArrayList<>();

    private CelestiumHoeClient() {
    }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            outlinePositions.clear();
            return;
        }

        updateOutline(client);
    }

    public static void render(LevelRenderContext context) {
        CelestiumPickaxeOutlineRenderer.render(context, outlinePositions);
    }

    private static void updateOutline(Minecraft client) {
        outlinePositions.clear();
        if (client.player == null || client.level == null || !CelestiumHoeHelper.isCelestiumHoe(client.player.getMainHandItem())) {
            return;
        }

        if (!(client.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }

        outlinePositions.addAll(CelestiumHoeHelper.getHarvestTargets(hitResult.getBlockPos(), client.level::getBlockState));
    }
}
